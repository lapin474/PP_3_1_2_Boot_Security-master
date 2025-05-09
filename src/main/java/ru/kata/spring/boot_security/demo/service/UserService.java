package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    void saveUser(User user);

    public void updateUser(Long id, User updatedUser, List<Long> roleIds);

    User showUser(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteUserById(Long id);

}
