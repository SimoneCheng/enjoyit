package com.enjoyit.repository;

import com.enjoyit.domain.GroupOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryGroupOrderRepositoryTest {

    private InMemoryGroupOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGroupOrderRepository();
    }

    @Test
    void testSaveAndFindById_ShouldStoreAndRetrieveOrder() {
        // Arrange
        GroupOrder order = new GroupOrder("測試團購A");
        order.setOrderId("order_123");

        // Act
        repository.save(order);
        Optional<GroupOrder> retrievedOrder = repository.findById("order_123");

        // Assert
        assertTrue(retrievedOrder.isPresent(), "應該要能找到剛剛存入的訂單");
        assertEquals("測試團購A", retrievedOrder.get().getOrderInfo());
    }

    @Test
    void testFindById_ShouldReturnEmptyIfNotFound() {
        // Act
        Optional<GroupOrder> result = repository.findById("non_exist_id");

        // Assert
        assertFalse(result.isPresent(), "找不到的訂單應該回傳 Empty Optional");
    }

    @Test
    void testFindAll_ShouldReturnAllSavedOrders() {
        // Arrange
        GroupOrder order1 = new GroupOrder("團購1");
        order1.setOrderId("id_1");
        GroupOrder order2 = new GroupOrder("團購2");
        order2.setOrderId("id_2");

        repository.save(order1);
        repository.save(order2);

        // Act
        List<GroupOrder> allOrders = repository.findAll();

        // Assert
        assertEquals(2, allOrders.size(), "應該回傳所有 2 筆訂單");
        assertTrue(allOrders.contains(order1));
        assertTrue(allOrders.contains(order2));
    }
}