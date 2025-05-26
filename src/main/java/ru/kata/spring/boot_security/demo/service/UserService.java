package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    void saveUser(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteUserById(Long id);

    void createUser(User user, List<Long> roleIds);

    void updateUser(String email, String firstName, String lastName, String newEmail, String password, List<Long> roleIds);

    Map<String, Object> getUserPageAttributes(String email);

    boolean userExistsByEmail(String email);

    User registerUser(User user, List<Long> roleIds);

    void autoAuthenticateUser(User user);

    String getRedirectPathByRole(User user);
}
