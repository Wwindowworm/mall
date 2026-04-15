package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.Product;
import com.mall.entity.ProductReview;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.ProductReviewMapper;
import com.mall.ai.ProductRecommendAgent;
import com.mall.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductReviewMapper reviewMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ProductRecommendAgent recommendAgent;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_TTL = 30;

    @Override
    public Page<Product> listProducts(Long categoryId, String keyword, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Product::getCategoryId, categoryId)
               .like(keyword != null, Product::getName, keyword)
               .eq(Product::getStatus, 1)
               .orderByDesc(Product::getSalesCount);
        Page<Product> p = new Page<>(page, size);
        productMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public Product getProductDetail(Long productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        Product cached = (Product) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("缓存命中 product={}", productId);
            return cached;
        }

        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (product.getStatus() == 0) {
            throw new BizException(ErrorCode.PRODUCT_OFF_SHELF);
        }

        redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
        return product;
    }

    @Override
    public List<Product> getRecommendProducts(Long userId) {
        // 用 DeepSeek AI 智能推荐
        List<Long> recommendIds = recommendAgent.recommend(userId, null, null, 10);
        if (recommendIds != null && !recommendIds.isEmpty()) {
            log.info("DeepSeek 推荐结果：userId={}, 推荐商品={}", userId, recommendIds);
            return productMapper.selectBatchIds(recommendIds);
        }
        // 降级：返回热门商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getSalesCount)
               .last("LIMIT 10");
        return productMapper.selectList(wrapper);
    }

    @Override
    public void addReview(Long userId, Long productId, Integer rating, String content, String images) {
        ProductReview review = new ProductReview();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);
    }
}
