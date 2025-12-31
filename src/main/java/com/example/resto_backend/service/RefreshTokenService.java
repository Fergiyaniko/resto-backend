package com.example.resto_backend.service;

import com.example.resto_backend.entity.RefreshToken;
import com.example.resto_backend.entity.User;
import com.example.resto_backend.repository.RefreshTokenRepository;
import com.example.resto_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create or replace refresh token for user
     */
    @Transactional
    public void create(User user, String token) {
        // Option A: delete old token then insert new
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(
                Instant.now().plusSeconds(7 * 24 * 60 * 60) // 7 days
        );

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validate refresh token
     */
    @Transactional(readOnly = true)
    public boolean validate(String username, String token) {
        Optional<RefreshToken> refreshTokenOpt =
                refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (!refreshToken.getUser().getUsername().equals(username)) {
            return false;
        }

        return refreshToken.getExpiryDate().isAfter(Instant.now());
    }

    @Transactional
    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUserId(user.getId());
    }
}

