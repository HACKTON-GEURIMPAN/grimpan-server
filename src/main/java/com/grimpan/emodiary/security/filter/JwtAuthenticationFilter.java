package com.grimpan.emodiary.security.filter;

import com.grimpan.emodiary.security.CustomUserDetail;
import com.grimpan.emodiary.security.CustomUserDetailService;
import com.grimpan.emodiary.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailService userDetailsService;

    private final String[] urls = { "/favicon.ico",
            "/api/v1/auth/kakao", "/api/v1/auth/google",
            "/api/v1/auth/apple", "/api/v1/auth/default",
            "/api/v1/auth/signup", "/api/v1/auth/reissue" };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtProvider.refineToken(request);
        Claims claims = jwtProvider.validateToken(token);

        String userid = claims.get("id").toString();

        CustomUserDetail userDetails = (CustomUserDetail) userDetailsService.loadUserByUsername(userid);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(urls).anyMatch(url -> url.equals(request.getRequestURI()));
    }
}