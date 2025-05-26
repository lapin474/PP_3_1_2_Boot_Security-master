package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserDetailsService userDetailsService; // Используем общий интерфейс
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(UserService userService,
                                  RoleService roleService,
                                  UserDetailsService userDetailsService, // Интерфейс вместо реализации
                                  PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // Открытие формы регистрации
    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "register"; // thymeleaf-шаблон register.html
    }

    // Обработка формы регистрации
    @PostMapping("/registration")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam List<Long> roleIds,
                               Model model) {

        // Проверка: существует ли уже пользователь с таким email
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            model.addAttribute("roles", roleService.getAllRoles());
            return "register";
        }

        // Шифруем пароль
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Устанавливаем роли
        user.setRoles(roleService.getRolesByIds(roleIds));

        // Сохраняем пользователя
        userService.saveUser(user);

        // Ручная аутентификация
        authenticateUser(user);

        // Перенаправление (можно кастомизировать)
        return redirectToHomePage(user);
    }

    private void authenticateUser(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String redirectToHomePage(User user) {
        // Тут можно реализовать логику в зависимости от ролей
        return "login"; // Или "redirect:/admin", "redirect:/user", и т.д.
    }
}
