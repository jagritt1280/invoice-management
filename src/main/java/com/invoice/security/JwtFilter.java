package com.invoice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = runs exactly once per HTTP request
    // before it reaches any controller

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1 — get Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2 — check if it has Bearer token
        // Authorization: Bearer eyJhbGc...
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            // no token → skip filter, security config handles it
            return;
        }

        // Step 3 — extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Step 4 — validate token and extract email
        if(jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);

            // Step 5 — load user details
            UserDetails userDetails = userDetailsService
                    .loadUserByUsername(email);

            // Step 6 — create authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            // Step 7 — set in security context
            // this is how Principal works in controllers
            // controller calls principal.getName()
            // Spring reads it from here
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // Step 8 — continue to next filter/controller
        filterChain.doFilter(request, response);
    }
}