package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.Admin;
import com.cyclonex.trust_care.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('ADMIN')")
    public Admin getAdminByUserId(@PathVariable int userId) {
        return adminService.getAdminByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('ADMIN')")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('ADMIN')")
    public Admin getAdminById(@PathVariable int id) {
        return adminService.getAdminById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADMIN') or hasRole('ADMIN')")
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminService.saveAdmin(admin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ADMIN') or hasRole('ADMIN')")
    public Admin updateAdmin(@PathVariable int id, @RequestBody Admin admin) {
        admin.setId(id);
        return adminService.saveAdmin(admin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_ADMIN') or hasRole('ADMIN')")
    public void deleteAdmin(@PathVariable int id) {
        adminService.deleteAdmin(id);
    }
}
