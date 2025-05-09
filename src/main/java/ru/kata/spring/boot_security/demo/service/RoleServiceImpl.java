package ru.kata.spring.boot_security.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Set<Role> getRolesByIds(List<Long> roleIds) {
        // Загружаем все роли по ID
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        // Проверяем, что количество загруженных ролей совпадает с количеством переданных ID
        if (roles.size() != roleIds.size()) {
            Set<Long> missingRoleIds = new HashSet<>(roleIds);
            for (Role role : roles) {
                missingRoleIds.remove(role.getId());
            }
            // Логируем отсутствующие роли
            logger.warn("Missing roles: {}", missingRoleIds);
            throw new IllegalArgumentException("Роли с ID " + missingRoleIds + " не найдены в базе данных.");
        }

        return roles; // Возвращаем множество ролей
    }



}
