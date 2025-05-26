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

    @GetMapping
    public String getAllUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + email));

        model.addAttribute("user", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("isAdmin", currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN")));
        return "user";
    }
//
//    @GetMapping("/new")
//    public String newUserForm(Model model) {
//        model.addAttribute("user", new User());
//        model.addAttribute("allRoles", roleService.getAllRoles());
//        return "user-form";
//    }
//
//    @GetMapping("/edit")
//    public String editUserForm(@RequestParam("id") Long id, Model model) {
//        model.addAttribute("user", userService.showUser(id));
//        model.addAttribute("allRoles", roleService.getAllRoles());
//        return "user-form";
//    }
//
//    @PostMapping("/create")
//    public String saveUser(@ModelAttribute User user,
//                           @RequestParam("roleIds") List<Long> roleIds,
//                           Model model) {
//        try {
//            userService.createUser(user, roleIds);
//            return "redirect:/users";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("allRoles", roleService.getAllRoles());
//            return "user-form";
//        }
//    }
//
//    @PostMapping("/update")
//    public String updateUser(@ModelAttribute User user,
//                             @RequestParam("roleIds") List<Long> roleIds,
//                             Model model) {
//        try {
//            userService.updateUser(user.getId(), user, roleIds);
//            return "redirect:/users";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("user", user);
//            model.addAttribute("allRoles", roleService.getAllRoles());
//            return "user-form";
//        }
//    }
//
//    @GetMapping("/delete")
//    public String deleteUser(@RequestParam("id") String email) {
//        userService.deleteUserByEmail(email);
//        return "redirect:/users";
//    }
//
//    @GetMapping("/detail")
//    public String showUser(@RequestParam("id") Long id, Model model) {
//        model.addAttribute("user", userService.showUser(id));
//        return "user-detail";
//    }
}
