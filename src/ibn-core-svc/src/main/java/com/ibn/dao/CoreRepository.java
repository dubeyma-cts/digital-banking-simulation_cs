package com.ibn.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ibn.core.entity.CoreEntity;

@Repository
public interface CoreRepository extends JpaRepository<CoreEntity, Long> {
    // Custom query methods can be defined here
}