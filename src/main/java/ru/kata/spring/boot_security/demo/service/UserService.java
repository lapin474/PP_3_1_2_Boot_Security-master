package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    void saveUser(User user);

    void deleteUserByEmail(String email);

    void updateUser(Long id, User updatedUser);

    User showUser(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void createUser(User user, List<Long> roleIds);
    void updateUser(String email, String firstName, String lastName, String newEmail, String password, List<Long> roleIds);
    void updateUser(Long id, User updatedUser, List<Long> roleIds);

}
