package com.example.resto_backend.controller;

import com.example.resto_backend.model.LoginRequest;
import com.example.resto_backend.model.RegisterRequest;
import com.example.resto_backend.entity.User;
import com.example.resto_backend.security.SecurityConstants;
import com.example.resto_backend.security.jwt.JwtUtil;
import com.example.resto_backend.service.AuthService;
import com.example.resto_backend.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
public class AuthPageController {

    private static final String LOGIN_PAGE = "login";
    private static final String REDIRECT_TO = "redirect:/";
    private static final String REGISTER_PAGE = "register";
    private static final String SWAGGER_PAGE = "swagger-ui/index.html";

    private static final String REDIRECT_TO_LOGIN_PAGE = REDIRECT_TO + LOGIN_PAGE;
    private static final String REDIRECT_TO_REGISTER_PAGE = REDIRECT_TO + REGISTER_PAGE;
    private static final String REDIRECT_TO_SWAGGER_PAGE = REDIRECT_TO + SWAGGER_PAGE;

    private static final String ERROR_REQUEST_ATTRIBUTE = "error";
    private static final String LOGIN_REQUEST_ATTRIBUTE = "loginRequest";
    private static final String REGISTER_REQUEST_ATTRIBUTE = "registerRequest";

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * Login Web Page
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = ERROR_REQUEST_ATTRIBUTE, required = false) String error, Model model, Authentication authentication) {
        model.addAttribute(LOGIN_REQUEST_ATTRIBUTE, new LoginRequest());

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return REDIRECT_TO_SWAGGER_PAGE;
        }
        if (error != null) {
            model.addAttribute("error", true);
        }
        return LOGIN_PAGE;
    }

    /**
     * Register Web Page
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("registerRequest", new RegisterRequest());
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return REDIRECT_TO_SWAGGER_PAGE;
        } else {
            return REGISTER_PAGE;
        }
    }

    /**
     * Handle Web Login
     */
    @PostMapping("/login")
    public String login(@ModelAttribute(LOGIN_REQUEST_ATTRIBUTE) LoginRequest req,
                        Model model,
                        HttpServletResponse response) {
        try {
            User user = authService.authenticate(new LoginRequest(req.username, req.password));
            String accessToken = jwtUtil.generateAccessToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            refreshTokenService.create(user, refreshToken);

            Cookie accessCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE, accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath(SecurityConstants.COOKIE_PATH);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie(SecurityConstants.REFRESH_TOKEN_COOKIE, refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath(SecurityConstants.COOKIE_PATH);
            response.addCookie(refreshCookie);

            return REDIRECT_TO_SWAGGER_PAGE;
        } catch (Exception e) {
            model.addAttribute(ERROR_REQUEST_ATTRIBUTE, true);
            return LOGIN_PAGE;
        }
    }

    /**
     * Handle Web Register
     */
    @PostMapping("/register")
    public String register(
            @ModelAttribute(REGISTER_REQUEST_ATTRIBUTE) RegisterRequest req,
            Model model) {

        try {
            authService.register(
                    new RegisterRequest(
                            req.getUsername(),
                            req.getPassword(),
                            req.getConfirmPassword(),
                            req.getEmail()
                    )
            );
            return REDIRECT_TO_LOGIN_PAGE;
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR_REQUEST_ATTRIBUTE, "Registration failed: " + e.getMessage());
            return REGISTER_PAGE;
        } catch (DataIntegrityViolationException dve) {
            model.addAttribute(ERROR_REQUEST_ATTRIBUTE, "Registration failed: " + "Username or email already exists");
            return REGISTER_PAGE;
        }
    }
}
