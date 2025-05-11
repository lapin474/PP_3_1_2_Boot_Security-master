package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Импортируем @Transactional
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true) // Чтение данных, не изменяем состояние
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true) // Чтение данных
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional
    public Set<Role> getRolesByIds(List<Long> roleIds) {
        // Загружаем все роли по ID
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        // Проверяем, что количество загруженных ролей совпадает с количеством переданных ID
        if (roles.size() != roleIds.size()) {
            // Если размеры не совпадают, значит, некоторые роли не найдены
            Set<Long> missingRoleIds = new HashSet<>(roleIds);
            for (Role role : roles) {
                missingRoleIds.remove(role.getId());
            }
            throw new IllegalArgumentException("Роли с ID " + missingRoleIds + " не найдены в базе данных.");
        }

        return roles;
    }
}
