package com.ibn.dao;

import com.ibn.core.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountDao extends JpaRepository<Account, UUID> {
    Optional<Account> findByCustomerId(UUID customerId);
    List<Account> findAllByCustomerId(UUID customerId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
