package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, RoleService roleService, UserRepository userRepository) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRepository = userRepository;
    }

    // Получение списка всех пользователей
    @GetMapping
    public String getAllUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();   // Получаем email текущего авторизованного пользователя
        User user = userService.findByEmail(email)  // Получаем пользователя по email
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + email));
        model.addAttribute("user", user);

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "user";
    }

    // Форма для создания нового пользователя
    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "user-form";
    }

    // Форма для редактирования пользователя
    @GetMapping("/edit")
    public String editUserForm(@RequestParam("id") Long id, Model model) {
        User user = userService.showUser(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "user-form";
    }

    // Сохранение нового пользователя
    @PostMapping("/create")
    public String saveUser(@ModelAttribute User user, @RequestParam("roleIds") List<Long> roleIds, Model model) {
        try {
            // Устанавливаем роли для пользователя
            user.setRoles(roleService.getRolesByIds(roleIds));
            userService.saveUser(user);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки, если роли не найдены
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user-form";  // Возвращаем форму с ошибкой
        }
        return "redirect:/users";
    }

    // Обновление пользователя
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, @RequestParam("roleIds") List<Long> roleIds, Model model) {
        try {
            // Получаем пользователя из базы по ID
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Проверка на уникальность email, если он был изменен
            if (!existingUser.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(user.getEmail())) {
                    throw new IllegalArgumentException("Пользователь с таким email уже существует");
                }
            }

            // Обновление только тех полей, которые изменены
            if (user.getFirstName() != null && !user.getFirstName().equals(existingUser.getFirstName())) {
                existingUser.setFirstName(user.getFirstName());
            }

            if (user.getLastName() != null && !user.getLastName().equals(existingUser.getLastName())) {
                existingUser.setLastName(user.getLastName());
            }

            // Обновляем роли
            existingUser.setRoles(roleService.getRolesByIds(roleIds));

            // Сохраняем изменения
            userRepository.save(existingUser);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user-form";  // Возвращаем форму с ошибкой
        }
        return "redirect:/users";
    }




    // Удаление пользователя
    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") String email) {
        userService.deleteUserByEmail(email);
        return "redirect:/users";
    }

    // Показать детали пользователя
    @GetMapping("/detail")
    public String showUser(@RequestParam("id") Long id, Model model) {
        User user = userService.showUser(id);
        model.addAttribute("user", user);
        return "user-detail";
    }

}
