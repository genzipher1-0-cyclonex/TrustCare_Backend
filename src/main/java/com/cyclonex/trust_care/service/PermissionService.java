package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.Permission;
import com.cyclonex.trust_care.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission getPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission savePermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public void deletePermission(int id) {
        permissionRepository.deleteById(id);
    }
}
