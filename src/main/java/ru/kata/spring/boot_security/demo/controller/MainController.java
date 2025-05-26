package ru.kata.spring.boot_security.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
public class MainController {

    private final UserService userService;
    private final RoleService roleService;

    public MainController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public String main(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login"; // или можно отобразить кастомную страницу ошибки
        }

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        model.addAttribute("user", user);
        return "main";
    }

    @GetMapping("/admin-content")
    public String getAdminContent(Model model,
                                  HttpServletRequest request,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        List<User> users = userService.getAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("allRoles", userService.getAllRoles());
        model.addAttribute("user", new User());
        if (isAjax(request)) {
            return "fragments/admin-content :: adminContent";
        }

        // обычный переход — возвращаем main.html, JS подгрузит фрагмент
        model.addAttribute("user", getUserFromDetails(userDetails));
        return "main";
    }

    @GetMapping("/user-page")
    public String getUserPage(Model model,
                              HttpServletRequest request,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        model.addAttribute("user", user);

        if (isAjax(request)) {
            return "fragments/user-page :: userContent";
        }

        return "main";
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isXmlHttp = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        boolean isNotHtml = accept == null || !accept.contains("text/html");
        return isXmlHttp || isNotHtml;
    }


    private User getUserFromDetails(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {
    userService.updateUser(user.getId(), user, roleIds);
    return "redirect:/";
    }
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            if (roleIds == null || roleIds.isEmpty()) {
                model.addAttribute("error", "Необходимо выбрать хотя бы одну роль");
                model.addAttribute("user", user);
                model.addAttribute("users", userService.getAllUsers());
                model.addAttribute("allRoles", userService.getAllRoles());
                return "main";
            }

            userService.createUser(user, roleIds);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно создан");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при создании пользователя: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("allRoles", userService.getAllRoles());
            return "main";
        }
    }

    @GetMapping("/api/roles")
    @ResponseBody
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
}
