package com.scrambled.recipeapp.repository;

import com.scrambled.recipeapp.model.UserSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByTokenHash(String tokenHash);
}
