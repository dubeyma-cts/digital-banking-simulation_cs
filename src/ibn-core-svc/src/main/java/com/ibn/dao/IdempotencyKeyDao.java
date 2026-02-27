package com.ibn.dao;

import com.ibn.core.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyKeyDao extends JpaRepository<IdempotencyKey, UUID> {
}
