package com.fraudengine.security;

import com.fraudengine.security.JwtExceptions.ExpiredTokenException;
import com.fraudengine.security.JwtExceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnBean(JwtService.class)
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login", "/actuator/health", "/v3/api-docs", "/v3/api-docs/", "/swagger-ui", "/swagger-ui/", "/swagger-ui.html"
    );

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // allow exact match and prefix matches for docs/swagger
        return PUBLIC_PATHS.stream().anyMatch(p -> uri.equals(p) || (p.endsWith("/") && uri.startsWith(p)) || (p.equals("/v3/api-docs") && uri.startsWith("/v3/api-docs")) || (p.equals("/swagger-ui") && uri.startsWith("/swagger-ui")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(response, "Missing Authorization header");
            return;
        }

        String token = header.substring(7);
        try {
            Jws<Claims> jws = jwtService.parseAndValidate(token);
            Claims claims = jws.getBody();
            if (claims.getExpiration() == null || claims.getExpiration().toInstant().isBefore(Instant.now())) {
                throw new ExpiredTokenException("Token expired");
            }
            String subject = claims.getSubject();
            Authentication auth = new UsernamePasswordAuthenticationToken(subject, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (ExpiredTokenException e) {
            unauthorized(response, e.getMessage());
        } catch (InvalidTokenException e) {
            unauthorized(response, e.getMessage());
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        String payload = toJson(Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", message
        ));
        response.getWriter().write(payload);
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var entry : map.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(entry.getKey()).append('"').append(':');
            Object v = entry.getValue();
            if (v instanceof Number) {
                sb.append(v);
            } else {
                sb.append('"').append(v.toString().replace("\"", "\\\"")).append('"');
            }
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }
}
