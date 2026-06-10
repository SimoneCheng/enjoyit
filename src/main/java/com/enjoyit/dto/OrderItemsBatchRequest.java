package com.enjoyit.dto;

import com.enjoyit.domain.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemsBatchRequest {
    private List<OrderItem> items = new ArrayList<>();

    public OrderItemsBatchRequest() {
    }

    public OrderItemsBatchRequest(List<OrderItem> items) {
        this.items = items;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
