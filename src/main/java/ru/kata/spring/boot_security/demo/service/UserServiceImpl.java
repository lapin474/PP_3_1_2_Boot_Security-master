package ru.kata.spring.boot_security.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    @Transactional
    public User createUser(User user, List<Long> roleIds) {
        // Проверка уникальности email
        checkEmailUniqueness(user.getEmail());

        // Шифруем пароль
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Получаем роли из базы данных
        Set<Role> roles = roleService.getRolesByIds(roleIds);  // Получаем Set<Role> вместо List<Role>

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        // Устанавливаем роли пользователю
        user.setRoles(roles);  // Устанавливаем роли как Set

        // Сохраняем пользователя в базе данных
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        // Проверка на уникальность email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Проверка на пустые поля (например, имя или email)
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Фамилия пользователя не может быть пустой");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email пользователя не может быть пустым");
        }

        // Проверка на пароль (например, минимальная длина)
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }

        // Шифрование пароля перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Сохранение пользователя в репозитории
        return userRepository.save(user);
    }
    @Override
    @Transactional
    public User saveUser(User user, List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ шифруем
        Set<Role> roles = roleService.getRolesByIds(roleIds);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User updatedUser, List<Long> roleIds) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));

        // Проверка на уникальность email
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            checkEmailUniqueness(updatedUser.getEmail());
        }

        // Обновление данных пользователя
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        // Если пароль изменен, шифруем его
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Обновление ролей пользователя
        existingUser.setRoles(roleService.getRolesByIds(roleIds));

        return userRepository.save(existingUser);  // Сохраняем обновленного пользователя
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);  // Удаляем пользователя по ID
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();  // Получаем всех пользователей
    }

    @Override
    public User showUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();  // Получаем все роли
    }

    private void checkEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public User registerUser(User user, List<Long> roleIds) {
        if (userExistsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleService.getRolesByIds(roleIds));
        saveUser(user);

        return user;
    }

    @Override
    public void autoAuthenticateUser(User user) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    @Override
    public String getRedirectPathByRole(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin" : "redirect:/users";
    }

}
