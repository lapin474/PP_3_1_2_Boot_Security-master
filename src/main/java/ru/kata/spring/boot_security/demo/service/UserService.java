package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    User saveUser(User user);

    User saveUser(User user, List<Long> roleIds);

    User createUser(User user, List<Long> roleIds);

    User updateUser(Long id, User updatedUser, List<Long> roleIds);

    void deleteUserById(Long id);

    Optional<User> findByEmail(String email);

    User showUser(Long id);

    List<Role> getAllRoles();

    boolean userExistsByEmail(String email);

    User registerUser(User user, List<Long> roleIds);

    void autoAuthenticateUser(User user);

    String getRedirectPathByRole(User user);
}
