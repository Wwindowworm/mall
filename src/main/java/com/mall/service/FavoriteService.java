package com.mall.service;

import com.mall.entity.Favorite;
import com.mall.entity.Product;
import java.util.List;

public interface FavoriteService {
    void add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    boolean isFavorited(Long userId, Long productId);
    List<Product> list(Long userId);
    Integer count(Long userId);
}
