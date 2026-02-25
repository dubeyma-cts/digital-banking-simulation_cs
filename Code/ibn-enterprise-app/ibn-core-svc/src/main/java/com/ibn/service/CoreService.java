package com.ibn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ibn.dao.CoreRepository;

@Service
public class CoreService {

    private final CoreRepository coreRepository;

    @Autowired
    public CoreService(CoreRepository coreRepository) {
        this.coreRepository = coreRepository;
    }

    // Business logic methods will be added here
}