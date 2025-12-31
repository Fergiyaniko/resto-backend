package com.example.resto_backend.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
    public String accessToken;
    public String errorMessage;
}
