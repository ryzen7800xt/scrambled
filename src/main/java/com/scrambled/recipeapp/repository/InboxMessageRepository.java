package com.scrambled.recipeapp.repository;

import com.scrambled.recipeapp.model.InboxMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, Long> {
    List<InboxMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<InboxMessage> findByIdAndUserId(Long id, Long userId);
}
