package ru.kata.spring.boot_security.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;


import java.util.Arrays;

@Service
public class RoleInitializationService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleInitializationService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Transactional
    public void initRoles() {
        // Проверяем, есть ли уже роли в базе данных
        if (roleRepository.count() == 0) {
            // Если ролей нет, создаем их
            Role adminRole = new Role("ROLE_ADMIN");
            Role userRole = new Role("ROLE_USER");

            roleRepository.saveAll(Arrays.asList(adminRole, userRole));

            System.out.println("Роли успешно добавлены в базу данных.");
        }
    }
}
