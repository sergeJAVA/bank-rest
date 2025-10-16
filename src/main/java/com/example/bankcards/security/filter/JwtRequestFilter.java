package com.example.bankcards.security.filter;

import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.security.TokenAuthentication;
import com.example.bankcards.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = TokenUtil.parseToken(request);

        if (token != null) {
            if (jwtService.isTokenExpired(token)) {
                sendErrorResponse(response);
                return;
            }
            TokenAuthentication authentication = new TokenAuthentication(jwtService.parseToken(token));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response)
            throws IOException {
        String message = "The token has expired or is invalid. Please log in again!";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}
