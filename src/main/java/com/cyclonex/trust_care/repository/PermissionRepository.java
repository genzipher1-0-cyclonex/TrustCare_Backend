package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Permission findByPermissionName(String permissionName);
}
