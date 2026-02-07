package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.Role;
import com.cyclonex.trust_care.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
