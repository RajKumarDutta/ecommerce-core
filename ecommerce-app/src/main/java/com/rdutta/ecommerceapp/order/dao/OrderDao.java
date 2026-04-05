package com.rdutta.ecommerceapp.order.dao;

import com.rdutta.ecommerceapp.order.dto.OrderItemRequest;
import com.rdutta.ecommerceapp.order.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderDao {
    OrderResponse createOrder(List<OrderItemRequest> items);
    OrderResponse confirmOrder(UUID orderId);
    void cancelOrder(UUID orderId);
}
