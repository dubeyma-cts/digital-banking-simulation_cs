package com.ibn.dao;

import com.ibn.core.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LoginAttemptDao extends JpaRepository<LoginAttempt, UUID> {
    long countByUsernameIgnoreCaseAndOutcomeAndAttemptAtAfter(String username, String outcome, LocalDateTime attemptAt);

    long countByUserIdAndOutcomeAndAttemptAtAfter(UUID userId, String outcome, LocalDateTime attemptAt);
}