package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.RolePermission;
import com.cyclonex.trust_care.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role-permissions")
@PreAuthorize("hasRole('ADMIN')")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @Autowired
    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/role/{roleId}")
    public List<RolePermission> getPermissionsByRole(@PathVariable int roleId) {
        return rolePermissionService.getPermissionsByRoleId(roleId);
    }

    @GetMapping("/permission/{permissionId}")
    public List<RolePermission> getRolesByPermission(@PathVariable int permissionId) {
        return rolePermissionService.getRolesByPermissionId(permissionId);
    }

    @GetMapping
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionService.getAllRolePermissions();
    }

    @PostMapping
    public RolePermission createRolePermission(@RequestBody RolePermission rolePermission) {
        return rolePermissionService.saveRolePermission(rolePermission);
    }

    @DeleteMapping("/{id}")
    public void deleteRolePermission(@PathVariable int id) {
        rolePermissionService.deleteRolePermission(id);
    }
}
