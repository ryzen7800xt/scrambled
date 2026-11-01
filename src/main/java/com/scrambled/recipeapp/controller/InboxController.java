package com.scrambled.recipeapp.controller;

import com.scrambled.recipeapp.model.InboxMessage;
import com.scrambled.recipeapp.model.User;
import com.scrambled.recipeapp.repository.InboxMessageRepository;
import com.scrambled.recipeapp.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/inbox")
public class InboxController {
    private final AuthService authService;
    private final InboxMessageRepository inboxRepository;

    public InboxController(AuthService authService, InboxMessageRepository inboxRepository) {
        this.authService = authService;
        this.inboxRepository = inboxRepository;
    }

    @GetMapping
    public List<InboxMessage> getMessages(
            @CookieValue(name = AuthService.SESSION_COOKIE, required = false) String token) {
        User user = authService.requireUser(token);
        return inboxRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @PostMapping("/{id}/read")
    public InboxMessage markRead(
            @PathVariable Long id,
            @CookieValue(name = AuthService.SESSION_COOKIE, required = false) String token) {
        User user = authService.requireUser(token);
        InboxMessage message = inboxRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found."));
        message.setRead(true);
        return inboxRepository.save(message);
    }
}
