package ru.kata.spring.boot_security.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService; // добавляем зависимость
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService; // внедряем зависимость
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Override
    @Transactional
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
    @Transactional
    public void createUser(User user, List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleService.getRolesByIds(roleIds));
        saveUser(user); // Сохраняем пользователя
    }

    @Override
    @Transactional
    public void updateUser(String email, String firstName, String lastName, String newEmail, String password, List<Long> roleIds) {
        User existingUser = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден: " + email));

        if (!existingUser.getEmail().equals(newEmail)) {
            if (existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }

        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setEmail(newEmail);

        if (password != null && !password.isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            existingUser.setRoles(roleService.getRolesByIds(roleIds));
        }

        saveUser(existingUser); // Сохраняем изменения
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Map<String, Object> getUserPageAttributes(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + email));

        List<User> users = getAllUsers();
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        attributes.put("users", users);
        attributes.put("isAdmin", isAdmin);
        return attributes;
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
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Override
    public String getRedirectPathByRole(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin" : "redirect:/users";
    }

}
