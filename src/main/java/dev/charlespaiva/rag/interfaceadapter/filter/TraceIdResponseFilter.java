package dev.charlespaiva.rag.interfaceadapter.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdResponseFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TraceIdResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Header must be set before the chain to ensure it is included before the response commits.
        Span span = tracer.currentSpan();
        String traceId = (span != null) ? span.context().traceId() : null;
        if (traceId == null || traceId.isBlank() || traceId.chars().allMatch(c -> c == '0')) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        response.setHeader("X-Trace-Id", traceId);
        filterChain.doFilter(request, response);
    }
}
