package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);
    List<User> findAll();

    User saveUser(User user);
    User saveUser(User user, List<Long> roleIds);

    User createUser(User user);
    User createUser(User user, List<Long> roleIds);
    User updateUser(String email, String firstName, String lastName, String newEmail,
                    String password, List<Long> roleIds);
    User updateUser(Long id, User user);
    User updateUser(Long id, User updatedUser, List<Long> roleIds);
    void deleteUserById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    User showUser(Long id);
    Map<String, Object> getUserPageAttributes(String email);
    List<Role> getAllRoles();
}
