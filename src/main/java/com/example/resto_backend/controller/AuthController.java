package com.example.resto_backend.controller;

import com.example.resto_backend.model.LoginResponse;
import com.example.resto_backend.model.LoginRequest;
import com.example.resto_backend.model.RegisterRequest;
import com.example.resto_backend.entity.User;
import com.example.resto_backend.security.SecurityConstants;
import com.example.resto_backend.security.jwt.JwtUtil;
import com.example.resto_backend.service.AuthService;
import com.example.resto_backend.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * API Login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) throws Exception {
        User user = authService.authenticate(request);
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        refreshTokenService.create(user, refreshToken);

        return ResponseEntity.ok(
                new LoginResponse(accessToken, "")
        );
    }

    /**
     * API Register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Refresh Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = SecurityConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.extractUsername(refreshToken, true);

        if (!refreshTokenService.validate(username, refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String newAccessToken = jwtUtil.generateAccessToken(username);

        Cookie accessCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE, newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath(SecurityConstants.COOKIE_PATH);
        response.addCookie(accessCookie);

        return ResponseEntity.ok(
                new LoginResponse(newAccessToken, "")
        );
    }

    /**
     * Handle Web Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = SecurityConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            String username = jwtUtil.extractUsername(refreshToken, true);
            refreshTokenService.deleteByUsername(username); // safer method
        }

        // Clear cookies
        Cookie accessCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE, null);
        accessCookie.setPath(SecurityConstants.COOKIE_PATH);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie(SecurityConstants.REFRESH_TOKEN_COOKIE, null);
        refreshCookie.setPath(SecurityConstants.COOKIE_PATH);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok().build();
    }

}
