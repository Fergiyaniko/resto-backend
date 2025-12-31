package com.example.resto_backend.repository;

import com.example.resto_backend.entity.Restaurant;
import com.example.resto_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByOwner(String username);

    boolean existsByOwner(User owner);
}

