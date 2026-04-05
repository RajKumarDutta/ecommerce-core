package com.rdutta.ecommerceapp.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdutta.ecommerceapp.common.dao.IdempotencyDao;
import com.rdutta.ecommerceapp.common.entity.Idempotency;
import com.rdutta.ecommerceapp.common.enums.IdempotencyStatus;
import com.rdutta.ecommerceapp.common.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IdempotencyDao {

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> T execute(String key, Class<T> clazz, IdempotentOperation<T> operation) {

        // 🔥 STEP 1: Try to fetch with lock
        var existingOpt = repository.findByIdForUpdate(key);

        if (existingOpt.isPresent()) {

            Idempotency existing = existingOpt.get();

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                return deserialize(existing.getResponse(), clazz);
            }

            // Already processing
            throw new RuntimeException("Request already in progress. Retry.");
        }

        // 🔥 STEP 2: Create PROCESSING entry
        Idempotency entity = repository.saveAndFlush(new Idempotency(key));

        // 🔥 STEP 3: Execute business logic
        T result = operation.execute();

        // 🔥 STEP 4: Mark completed
        try {
            String json = objectMapper.writeValueAsString(result);
            entity.markCompleted(json);
            repository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store response");
        }

        return result;
    }
    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response");
        }
    }
}