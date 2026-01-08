package com.accessrequest.api.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Interceptor for logging HTTP requests and responses.
 * Adds request tracing and performance monitoring.
 */
@Slf4j
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * Pre-handle: Log incoming request and add trace ID.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or retrieve trace ID
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // Store trace ID in request attribute
        request.setAttribute(TRACE_ID_HEADER, traceId);

        // Store start time for performance monitoring
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        // Log incoming request
        log.info("[{}] {} {} - User-Agent: {}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("User-Agent"));

        // Add trace ID to response header
        response.addHeader(TRACE_ID_HEADER, traceId);

        return true;
    }

    /**
     * After completion: Log response and performance metrics.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String traceId = (String) request.getAttribute(TRACE_ID_HEADER);
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            if (ex != null) {
                log.error("[{}] {} {} - Status: {} - Duration: {}ms - Error: {}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration,
                        ex.getMessage());
            } else {
                log.info("[{}] {} {} - Status: {} - Duration: {}ms",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration);
            }
        }
    }
}
