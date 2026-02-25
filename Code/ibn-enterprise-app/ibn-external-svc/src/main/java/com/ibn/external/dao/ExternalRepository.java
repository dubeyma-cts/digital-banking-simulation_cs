package com.ibn.external.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ibn.external.model.ExternalEntity;

@Repository
public interface ExternalRepository extends JpaRepository<ExternalEntity, Long> {
    // Custom query methods can be defined here
}