package ru.kata.spring.boot_security.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService; // добавляем зависимость

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService; // внедряем зависимость
    }

    @Override
    public void saveUser(User user) {
        if (user.getId() != null) { // Если пользователь уже существует в базе данных, то проверяем уникальность email
            User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            if (!existingUser.getEmail().equals(user.getEmail())) { // Если email был изменён, проверяем его уникальность
                Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());

                if (userWithSameEmail.isPresent()) {
                    throw new IllegalArgumentException("Пользователь с таким email уже существует");
                }
            }
        }

        userRepository.save(user); // Если пользователя не было в базе данных или его email изменился, то сохраняем его
    }




    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }



    @Override
    @Transactional
    public void updateUser(Long id, User updatedUser, List<Long> roleIds) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            // Если email был изменён, проверяем его уникальность
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                Optional<User> userWithSameEmail = userRepository.findByEmail(updatedUser.getEmail());
                if (userWithSameEmail.isPresent()) {
                    throw new IllegalArgumentException("Email уже используется другим пользователем");
                }
            }

            // Обновляем основные поля
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setEmail(updatedUser.getEmail());

            // Обновляем пароль, если он был передан
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            // Обновляем роли, если они переданы
            if (roleIds != null && !roleIds.isEmpty()) {
                // Получаем роли по ID
                Set<Role> roles = roleService.getRolesByIds(roleIds);

                // Обновляем роли пользователя
                existingUser.setRoles(roles);
            }

            userRepository.save(existingUser); // Сохраняем изменения
        } else {
            throw new IllegalArgumentException("Пользователь с ID " + id + " не найден");
        }
    }








    @Override
    public User showUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Пользователь с ID " + id + " не найден"));
    }
    @Override
    // Метод для поиска пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
