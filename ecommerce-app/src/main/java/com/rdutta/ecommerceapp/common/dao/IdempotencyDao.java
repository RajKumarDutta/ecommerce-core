package com.rdutta.ecommerceapp.common.dao;

public interface IdempotencyDao {

    <T> T execute(String key, Class<T> clazz, IdempotentOperation<T> operation);

    @FunctionalInterface
    interface IdempotentOperation<T> {
        T execute();
    }
}