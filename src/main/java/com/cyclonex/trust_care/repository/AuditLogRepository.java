package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByUser_Id(Integer userId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByResource(String resource);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
