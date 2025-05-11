package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    // Главная страница администратора
    @GetMapping
    public String showAdminPage() {
        return "admin";
    }

    // Список всех пользователей
    @GetMapping("/users")
    public String viewUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // Страница создания нового пользователя
    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/create-user";
    }

    // Обработка создания нового пользователя
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam List<Long> roleIds) {
        userService.createUser(user, roleIds);
        return "redirect:/admin/users";
    }

    // Страница редактирования пользователя
    @GetMapping("/users/{email}/edit")
    public String showEditUserForm(@PathVariable String email, Model model) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с email " + email + " не найден"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/edit-user";
    }

    // Обработка редактирования пользователя
    @PostMapping("/users/{email}")
    public String updateUser(@PathVariable String email,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String newEmail,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<Long> roleIds) {
        userService.updateUser(email, firstName, lastName, newEmail, password, roleIds);
        return "redirect:/admin/users";
    }

    // Удаление пользователя
    @GetMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return "redirect:/admin/users";
    }
}
