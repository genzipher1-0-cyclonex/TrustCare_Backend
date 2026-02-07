package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    List<RolePermission> findByRoleId(int roleId);
    List<RolePermission> findByPermissionId(int permissionId);
}
