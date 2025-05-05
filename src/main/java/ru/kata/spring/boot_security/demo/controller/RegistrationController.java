package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserDetailsServiceImpl;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder; // Импортируем

import java.util.List;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder; // Добавляем поле для PasswordEncoder

    // Внедряем зависимость через конструктор
    @Autowired
    public RegistrationController(UserService userService, RoleService roleService,
                                  UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder; // Инициализируем
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
        // Проверка на существование пользователя с таким email
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            model.addAttribute("roles", roleService.getAllRoles());
            return "register";  // Возвращаем форму регистрации с сообщением об ошибке
        }

        // Шифруем пароль перед сохранением
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword); // Устанавливаем зашифрованный пароль

        System.out.println("Encoded password: " + encodedPassword);  // Для отладки

        // Устанавливаем роли пользователя
        user.setRoles(roleService.getRolesByIds(roleIds));

        // Сохраняем пользователя
        userService.saveUser(user);

        // Ручная аутентификация
        authenticateUser(user);

        // Переход на страницу в зависимости от роли
        return redirectToHomePage(user);
    }

    private void authenticateUser(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String redirectToHomePage(User user) {
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        } else {
            return "redirect:/users";
        }
    }
}
