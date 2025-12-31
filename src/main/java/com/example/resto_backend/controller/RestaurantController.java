package com.example.resto_backend.controller;

import com.example.resto_backend.entity.Restaurant;
import com.example.resto_backend.entity.User;
import com.example.resto_backend.repository.RestaurantRepository;
import com.example.resto_backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Restaurant", description = "Operations related to restaurant management")
@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public Restaurant getMyRestaurant(Authentication authentication) {

        String username = authentication.getName();

        return restaurantRepository
                .findByOwner(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }
}
