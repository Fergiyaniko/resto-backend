package com.example.resto_backend.service;

import com.example.resto_backend.entity.Restaurant;
import com.example.resto_backend.exception.InvalidUsernameOrPasswordException;
import com.example.resto_backend.model.LoginRequest;
import com.example.resto_backend.model.RegisterRequest;
import com.example.resto_backend.entity.User;
import com.example.resto_backend.model.Role;
import com.example.resto_backend.repository.RestaurantRepository;
import com.example.resto_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RestaurantRepository restaurantRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginRequest request) throws Exception {
        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new InvalidUsernameOrPasswordException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new InvalidUsernameOrPasswordException("Invalid username or password");
        }

        return user;
    }

    @Transactional
    public User register(RegisterRequest req) {
        validateRequest(req);

        Restaurant restaurant = createRestaurant(req);
        User user = createUser(req, restaurant);

        return userRepository.save(user);
    }

    private void validateRequest(RegisterRequest req) throws IllegalArgumentException {
        String error = "";

        // Validation: minimum password length
        if (req.getUsername().length() < 6) {
            error = "Username must be at least 6 characters long";
        }

        // Validation: minimum password length
        if (req.getPassword().length() < 6) {
            error = "Password must be at least 6 characters long";
        }

        // Validation: password confirmation
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            error = "Passwords do not match";
        }

        // Validation: only Gmail addresses
        if (!req.getEmail().toLowerCase().endsWith("@gmail.com")) {
            error = "Only Gmail addresses are allowed";
        }

        if(!error.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    private Restaurant createRestaurant(RegisterRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getUsername() + "'s Restaurant");
        restaurant.setOwner(request.getUsername());
        return restaurantRepository.save(restaurant);
    }

    private User createUser(RegisterRequest request, Restaurant restaurant) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.OWNER);
        user.setRestaurant(restaurant);
        return user;
    }

}

