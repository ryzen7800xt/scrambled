package com.scrambled.recipeapp.controller;

import com.scrambled.recipeapp.model.User;
import com.scrambled.recipeapp.service.AuthService;
import com.scrambled.recipeapp.service.AuthService.SessionCredentials;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final long SESSION_SECONDS = 30L * 24 * 60 * 60;

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            @Value("${recipeapp.auth.cookie-secure:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody Credentials credentials, HttpServletResponse response) {
        return setSession(
                authService.register(credentials.username(), credentials.email(), credentials.password()),
                response);
    }

    @PostMapping("/login")
    public User login(@Valid @RequestBody Credentials credentials, HttpServletResponse response) {
        return setSession(authService.login(credentials.email(), credentials.password()), response);
    }

    @GetMapping("/me")
    public User currentUser(@CookieValue(name = AuthService.SESSION_COOKIE, required = false) String token) {
        return authService.findCurrentUser(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in."));
    }

    @PostMapping("/logout")
    public void logout(
            @CookieValue(name = AuthService.SESSION_COOKIE, required = false) String token,
            HttpServletResponse response) {
        authService.logout(token);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString());
    }

    private User setSession(SessionCredentials credentials, HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                sessionCookie(credentials.token(), SESSION_SECONDS).toString());
        return credentials.user();
    }

    private ResponseCookie sessionCookie(String value, long maxAge) {
        return ResponseCookie.from(AuthService.SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public record Credentials(
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(max = 254)
            @jakarta.validation.constraints.Email
            String email,
            String username,
            @jakarta.validation.constraints.NotBlank String password) { }
}
