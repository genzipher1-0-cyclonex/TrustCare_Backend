package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
     Role findByRoleName(String roleName);
}
