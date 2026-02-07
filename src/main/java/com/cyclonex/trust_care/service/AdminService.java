package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.Admin;
import com.cyclonex.trust_care.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin getAdminByUserId(int userId) {
        return adminRepository.findByUser_Id(userId);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public void deleteAdmin(int id) {
        adminRepository.deleteById(id);
    }

    public Admin getAdminById(int id) {
        return adminRepository.findById(id).orElse(null);
    }
}
