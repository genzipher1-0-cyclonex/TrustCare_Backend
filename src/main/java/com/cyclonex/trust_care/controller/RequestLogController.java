package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.RequestLog;
import com.cyclonex.trust_care.service.RequestLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/request_log")
public class RequestLogController {

    private final RequestLogService requestLogService;

    @Autowired
    public RequestLogController(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @GetMapping
    public List<RequestLog> getAllRequestLogs() {
        return requestLogService.getAllRequestLogs();
    }

    @GetMapping("/{id}")
    public RequestLog getRequestLogById(@PathVariable int id) {
        return requestLogService.getRequestLogById(id);
    }

    @GetMapping("/request-id/{requestId}")
    public RequestLog getRequestLogByRequestId(@PathVariable String requestId) {
        return requestLogService.getRequestLogByRequestId(requestId);
    }

    @GetMapping("/user/{userId}")
    public List<RequestLog> getRequestLogsByUserId(@PathVariable Integer userId) {
        return requestLogService.getRequestLogsByUserId(userId);
    }

    @GetMapping("/operation/{operation}")
    public List<RequestLog> getRequestLogsByOperation(@PathVariable String operation) {
        return requestLogService.getRequestLogsByOperation(operation);
    }

    @GetMapping("/outcome/{outcome}")
    public List<RequestLog> getRequestLogsByOutcome(@PathVariable String outcome) {
        return requestLogService.getRequestLogsByOutcome(outcome);
    }

    @GetMapping("/date-range")
    public List<RequestLog> getRequestLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return requestLogService.getRequestLogsByDateRange(start, end);
    }

    @PostMapping
    public RequestLog createRequestLog(@RequestBody RequestLog requestLog) {
        return requestLogService.saveRequestLog(requestLog);
    }

    @DeleteMapping("/{id}")
    public void deleteRequestLog(@PathVariable int id) {
        requestLogService.deleteRequestLog(id);
    }
}
