package com.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.Product;

import java.util.List;

public interface ProductService {
    Page<Product> listProducts(Long categoryId, String keyword, int page, int size);
    Product getProductDetail(Long productId);
    List<Product> getRecommendProducts(Long userId);
    void addReview(Long userId, Long productId, Integer rating, String content, String images);
}
