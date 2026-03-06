package com.mapic.backend.config;

import com.mapic.backend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/uploads/") || path.startsWith("/api/auth/");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            
            log.info("Request URI: {}, Auth Header: {}", request.getRequestURI(), header != null ? "Present" : "Missing");
            
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                log.info("Token extracted, length: {}", token.length());
                
                if (jwtUtil.validateToken(token)) {
                    Long accountId = jwtUtil.getAccountIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    
                    log.info("Token valid - AccountId: {}, Role: {}", accountId, role);
                    
                    // Set authentication with accountId as principal
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            accountId.toString(), 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authentication set successfully");
                } else {
                    log.warn("Token validation failed");
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}
