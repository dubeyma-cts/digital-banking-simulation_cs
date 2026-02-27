package com.ibn.dao;

import com.ibn.core.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountBalanceDao extends JpaRepository<AccountBalance, UUID> {
}
