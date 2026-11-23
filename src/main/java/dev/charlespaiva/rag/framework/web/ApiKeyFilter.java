package dev.charlespaiva.rag.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String PROTECTED_PATH_PREFIX = "/api/";

    private final String expectedKey;

    public ApiKeyFilter(@Value("${rag.security.api-key:}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (expectedKey.isBlank() || !request.getRequestURI().startsWith(PROTECTED_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        if (expectedKey.equals(request.getHeader(API_KEY_HEADER))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"detail\":\"valid X-Api-Key header is required\"}");
    }
}
