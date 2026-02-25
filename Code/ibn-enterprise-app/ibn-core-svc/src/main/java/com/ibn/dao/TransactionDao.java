package com.ibn.dao;

import com.ibn.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionDao extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountIdOrderByPostedAtDesc(UUID accountId);

    List<Transaction> findByAccountIdAndValueDateBetweenOrderByPostedAtDesc(
            UUID accountId,
            LocalDate fromDate,
            LocalDate toDate);
}
