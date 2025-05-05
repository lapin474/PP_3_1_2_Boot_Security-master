package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // добавляем

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(User user) {
        if (user.getId() != null) {
            User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            // Если email не изменился, пропускаем проверку уникальности
            if (!existingUser.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(user.getEmail())) {
                    throw new IllegalArgumentException("Пользователь с таким email уже существует");
                }
            }
        }
        userRepository.save(user);
    }



    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден: " + email));
        userRepository.delete(user); // Удаляем пользователя
    }


    @Override
    public void updateUser(Long id, User updatedUser) {
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
            if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                existingUser.setRoles(updatedUser.getRoles());
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

    // Метод для поиска пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
