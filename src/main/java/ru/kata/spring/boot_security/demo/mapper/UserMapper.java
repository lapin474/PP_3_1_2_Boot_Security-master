package ru.kata.spring.boot_security.demo.mapper;


import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPassword(null);
        dto.setRoleIds(user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList()));
        dto.setRoleIds(user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList()));

        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));

        return dto;
    }

    public User fromDTO(UserDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }
}
