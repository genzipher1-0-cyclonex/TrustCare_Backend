package com.cyclonex.trust_care.interceptor;

import com.cyclonex.trust_care.entity.RequestLog;
import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.UserRepository;
import com.cyclonex.trust_care.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private final RequestLogService requestLogService;
    private final UserRepository userRepository;

    @Autowired
    public RequestLoggingInterceptor(RequestLogService requestLogService, UserRepository userRepository) {
        this.requestLogService = requestLogService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("requestId", UUID.randomUUID().toString());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestId = (String) request.getAttribute("requestId");
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        int responseStatus = response.getStatus();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            user = userRepository.findByEmail(email);
        }

        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(requestId);
        requestLog.setUser(user);
        requestLog.setOperation(method + " " + endpoint);
        requestLog.setOutcome(responseStatus >= 200 && responseStatus < 300 ? "success" : "failure");

        requestLogService.saveRequestLog(requestLog);
    }
}
