package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // Получение списка всех пользователей
    @GetMapping
    public String getAllUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> attributes = userService.getUserPageAttributes(userDetails.getUsername());
        model.addAllAttributes(attributes);
        return "user";
    }

    // Форма для редактирования пользователя
    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", userService.getAllRoles());
        return "user-form";
    }

    @GetMapping("/edit")
    public String editUserForm(@RequestParam("id") Long id, Model model) {
        model.addAttribute("user", userService.showUser(id));
        model.addAttribute("allRoles", userService.getAllRoles());
        return "user-form";
    }

    @PostMapping("/create")
    public String saveUser(@ModelAttribute User user, @RequestParam("roleIds") List<Long> roleIds, Model model) {
        try {
            userService.createUser(user, roleIds);
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allRoles", userService.getAllRoles());
            return "user-form";
        }
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, @RequestParam("roleIds") List<Long> roleIds, Model model) {
        try {
            userService.updateUser(user, roleIds);
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("allRoles", userService.getAllRoles());
            return "user-form";
        }
    }

    // Удаление пользователя
    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") long id) {
        userService.deleteUserById(id);
        return "redirect:/users";
    }

}
