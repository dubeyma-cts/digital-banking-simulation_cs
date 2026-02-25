package com.ibn.dao;

import com.ibn.core.entity.ChequeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChequeDepositDao extends JpaRepository<ChequeDeposit, UUID> {
    List<ChequeDeposit> findAllByCustomerIdOrderBySubmittedAtDesc(UUID customerId);
    List<ChequeDeposit> findAllByOrderBySubmittedAtDesc();
}
