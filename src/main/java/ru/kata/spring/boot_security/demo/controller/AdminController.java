package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Добавляем для использования @PreAuthorize
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
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @GetMapping
    public String showAdminPage() {
        return "admin";
    }

    // Список всех пользователей
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @GetMapping("/users")
    public String viewUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // Страница создания нового пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/create-user";
    }

    // Обработка создания нового пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleService.getRolesByIds(roleIds));
        userService.saveUser(user);
        return "redirect:/admin/users";
    }

    // Страница редактирования пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @GetMapping("/users/{email}/edit")
    public String showEditUserForm(@PathVariable String email, Model model) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с email " + email + " не найден"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/edit-user";
    }

    // Обработка редактирования пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @PostMapping("/users/{email}")
    public String updateUser(@PathVariable String email,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String newEmail,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<Long> roleIds) {

        // Получаем пользователя по старому email
        User existingUser = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден: " + email));

        // Проверяем, был ли изменен email
        if (!existingUser.getEmail().equals(newEmail)) {
            // Если email был изменен, проверяем на уникальность, исключая текущего пользователя
            if (userService.existsByEmail(newEmail) && !existingUser.getEmail().equals(newEmail)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }

        // Обновляем значения полей
        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setEmail(newEmail); // можно обновить email, если он не совпадает с текущим

        // Если пароль был передан, обновляем его
        if (password != null && !password.isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        // Обновляем роли, если они были изменены
        if (roleIds != null && !roleIds.isEmpty()) {
            existingUser.setRoles(roleService.getRolesByIds(roleIds));
        }

        // Сохраняем изменения
        userService.saveUser(existingUser);

        return "redirect:/admin/users";
    }

    // Удаление пользователя
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Используем @PreAuthorize для ограничения доступа
    @GetMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return "redirect:/admin/users";
    }
}
