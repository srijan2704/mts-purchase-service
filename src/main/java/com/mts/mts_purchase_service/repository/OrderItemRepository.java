package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for order items.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
