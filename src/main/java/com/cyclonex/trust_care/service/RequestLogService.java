package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.RequestLog;
import com.cyclonex.trust_care.repository.RequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    @Autowired
    public RequestLogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public RequestLog saveRequestLog(RequestLog requestLog) {
        if (requestLog.getRequestId() == null || requestLog.getRequestId().isEmpty()) {
            requestLog.setRequestId(UUID.randomUUID().toString());
        }
        return requestLogRepository.save(requestLog);
    }

    public List<RequestLog> getAllRequestLogs() {
        return requestLogRepository.findAll();
    }

    public RequestLog getRequestLogByRequestId(String requestId) {
        return requestLogRepository.findByRequestId(requestId);
    }

    public List<RequestLog> getRequestLogsByUserId(Integer userId) {
        return requestLogRepository.findByUser_Id(userId);
    }

    public List<RequestLog> getRequestLogsByOperation(String operation) {
        return requestLogRepository.findByOperation(operation);
    }

    public List<RequestLog> getRequestLogsByOutcome(String outcome) {
        return requestLogRepository.findByOutcome(outcome);
    }

    public List<RequestLog> getRequestLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return requestLogRepository.findByTimestampBetween(start, end);
    }

    public RequestLog getRequestLogById(int id) {
        return requestLogRepository.findById(id).orElse(null);
    }

    public void deleteRequestLog(int id) {
        requestLogRepository.deleteById(id);
    }
}
