package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Integer> {
    RequestLog findByRequestId(String requestId);
    List<RequestLog> findByUser_Id(Integer userId);
    List<RequestLog> findByOperation(String operation);
    List<RequestLog> findByOutcome(String outcome);
    List<RequestLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
