package com.ibn.dao;

import com.ibn.core.entity.ChequeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChequeStatusHistoryDao extends JpaRepository<ChequeStatusHistory, UUID> {
}
