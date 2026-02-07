package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.RolePermission;
import com.cyclonex.trust_care.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public RolePermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public List<RolePermission> getPermissionsByRoleId(int roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public List<RolePermission> getRolesByPermissionId(int permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    public RolePermission saveRolePermission(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    public void deleteRolePermission(int id) {
        rolePermissionRepository.deleteById(id);
    }

    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }
}
