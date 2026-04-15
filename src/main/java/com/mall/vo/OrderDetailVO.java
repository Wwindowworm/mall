package com.mall.vo;

import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public class OrderDetailVO {

    private Order order;
    private List<OrderItem> items;

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
