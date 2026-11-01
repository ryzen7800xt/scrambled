package com.scrambled.recipeapp.service;

import com.scrambled.recipeapp.model.User;
import com.scrambled.recipeapp.model.UserSession;
import com.scrambled.recipeapp.model.InboxMessage;
import com.scrambled.recipeapp.repository.InboxMessageRepository;
import com.scrambled.recipeapp.repository.UserRepository;
import com.scrambled.recipeapp.repository.UserSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    public static final String SESSION_COOKIE = "SCRAMBLED_SESSION";
    private static final int SESSION_DAYS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?"
                    + "(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final InboxMessageRepository inboxRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            InboxMessageRepository inboxRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.inboxRepository = inboxRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SessionCredentials register(String username, String email, String password) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 12 characters and no more than 72 UTF-8 bytes.");
        }
        if (normalizedEmail.length() > 254 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
        if (!normalizedUsername.matches("[a-z0-9._-]{3,30}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username must be 3–30 characters and use letters, numbers, dots, underscores, or hyphens.");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That email address is already registered.");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        addInboxMessage(savedUser, "Welcome to Scrambled! Your account is ready.");
        return createSession(savedUser);
    }

    @Transactional
    public SessionCredentials login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        }
        return createSession(user);
    }

    @Transactional
    public User requireUser(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        UserSession session = sessionRepository.findByTokenHash(hashToken(rawToken))
                .filter(found -> found.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Your session has expired. Please sign in again."));
        return session.getUser();
    }

    @SuppressWarnings("null")
    @Transactional
    public Optional<User> findCurrentUser(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByTokenHash(hashToken(rawToken))
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .map(UserSession::getUser);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            sessionRepository.findByTokenHash(hashToken(rawToken)).ifPresent(sessionRepository::delete);
        }
    }

    private SessionCredentials createSession(User user) {
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        UserSession session = new UserSession();
        session.setTokenHash(hashToken(rawToken));
        session.setExpiresAt(Instant.now().plus(SESSION_DAYS, ChronoUnit.DAYS));
        session.setUser(user);
        sessionRepository.save(session);
        return new SessionCredentials(user, rawToken);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void addInboxMessage(User user, String text) {
        InboxMessage message = new InboxMessage();
        message.setMessage(text);
        message.setCreatedAt(Instant.now());
        message.setRead(false);
        message.setUser(user);
        inboxRepository.save(message);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    public record SessionCredentials(User user, String token) { }
}
