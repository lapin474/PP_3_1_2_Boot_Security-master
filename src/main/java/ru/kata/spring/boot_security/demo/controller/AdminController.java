package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Добавляем для использования @PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;
import java.util.Set;

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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showAdminPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("user", new User()); // для формы создания нового пользователя
        return "admin";
    }

    // Обработка создания нового пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleService.getRolesByIds(roleIds));
        userService.saveUser(user);
        return "redirect:/admin";
    }
//
//    // Страница редактирования пользователя
//    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
//    @GetMapping("/users/{email}/edit")
//    public String showEditUserForm(@PathVariable String email, Model model) {
//        User user = userService.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("Пользователь с email " + email + " не найден"));
//        model.addAttribute("user", user);
//        model.addAttribute("roles", roleService.getAllRoles());
//        return "admin/edit-user";
//    }

    // Обработка редактирования пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @PostMapping("/users/{email}")
    public String updateUser(@PathVariable String email,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String newEmail,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<Long> roleIds) {

        try {
            User existingUser = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден: " + email));

            existingUser.setFirstName(firstName);
            existingUser.setLastName(lastName);
            existingUser.setEmail(newEmail);

            if (password != null && !password.isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(password));
            }

            if (roleIds != null && !roleIds.isEmpty()) {
                Set<Role> roles = roleService.getRolesByIds(roleIds);
                existingUser.setRoles(roles);
            }

            userService.saveUser(existingUser);
        } catch (IllegalArgumentException ex) {
            // Логируем ошибку
            System.out.println("Ошибка обновления ролей: " + ex.getMessage());
            return "redirect:/admin?error=" + ex.getMessage();
        }

        return "redirect:/admin";
    }


    // Удаление пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin";
    }

}
