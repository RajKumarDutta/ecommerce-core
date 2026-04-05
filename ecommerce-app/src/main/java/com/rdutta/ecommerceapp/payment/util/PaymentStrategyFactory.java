package com.rdutta.ecommerceapp.payment.util;

import com.rdutta.ecommerceapp.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> list) {
        this.strategies = list.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getProvider,
                        Function.identity()
                ));
    }

    public PaymentStrategy get(String provider) {
        return Optional.ofNullable(strategies.get(provider))
                .orElseThrow(() -> new RuntimeException("Invalid provider"));
    }
}