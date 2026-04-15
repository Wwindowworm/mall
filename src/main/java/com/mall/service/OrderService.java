package com.mall.service;

import com.mall.entity.Order;
import com.mall.vo.OrderDetailVO;
import java.util.List;

public interface OrderService {
    Order createFromCart(Long userId, Long addressId, String remark);
    Order createDirect(Long userId, Long productId, Integer quantity, Long addressId, String remark);
    OrderDetailVO getDetail(Long userId, Long orderId);
    List<Order> list(Long userId, Integer status);
    void cancel(Long userId, Long orderId);
    void pay(Long userId, String orderNo);
    void confirmReceive(Long userId, Long orderId);
}
