package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Admin findByUser_Id(int userId);
}
