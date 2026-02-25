package com.ibn.dao;

import com.ibn.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDao extends JpaRepository<User, UUID> {
    Optional<User> findByCustomerId(UUID customerId);
    Optional<User> findByUsernameIgnoreCase(String username);
}
