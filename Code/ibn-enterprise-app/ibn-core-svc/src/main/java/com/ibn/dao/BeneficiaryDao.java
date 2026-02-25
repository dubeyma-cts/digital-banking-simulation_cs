package com.ibn.dao;

import com.ibn.core.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryDao extends JpaRepository<Beneficiary, UUID> {
    Optional<Beneficiary> findByCustomerIdAndAccountNumber(UUID customerId, String accountNumber);
}
