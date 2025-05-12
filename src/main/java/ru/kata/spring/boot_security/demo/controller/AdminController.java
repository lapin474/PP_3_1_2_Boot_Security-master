package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.service.RoleService;

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
    public String showAdminPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("user", new User()); // для формы создания нового пользователя
        return "admin";
    }

    // Обработка создания нового пользователя
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user, @RequestParam List<Long> roleIds) {
        userService.createUser(user, roleIds);
        return "redirect:/admin";
    }

    // Обработка редактирования пользователя
    @PostMapping("/users/{email}")
    public String updateUser(@PathVariable String email,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String newEmail,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<Long> roleIds) {
        try {
            userService.updateUser(email, firstName, lastName, newEmail, password, roleIds);
        } catch (IllegalArgumentException ex) {
            // Логируем ошибку
            System.out.println("Ошибка обновления ролей: " + ex.getMessage());
            return "redirect:/admin?error=" + ex.getMessage();
        }
        return "redirect:/admin";
    }

    // Удаление пользователя
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin";
    }
}
