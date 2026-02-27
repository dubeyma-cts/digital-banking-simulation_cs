package com.ibn.dao;

import com.ibn.core.entity.TransferInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferInstructionDao extends JpaRepository<TransferInstruction, UUID> {
}
