package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.Permission;
import com.cyclonex.trust_care.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{permissionName}")
    public Permission getPermissionByName(@PathVariable String permissionName) {
        return permissionService.getPermissionByName(permissionName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionService.savePermission(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable int id) {
        permissionService.deletePermission(id);
    }
}
