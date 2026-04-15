package com.mall.service;

import com.mall.vo.CartItemVO;
import java.util.List;

public interface CartService {
    void add(Long userId, Long productId, Integer quantity);
    void remove(Long userId, Long productId);
    void updateQuantity(Long userId, Long productId, Integer quantity);
    void clear(Long userId);
    List<CartItemVO> list(Long userId);
    Integer count(Long userId);
}
