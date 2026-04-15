package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.Favorite;
import com.mall.entity.Product;
import com.mall.mapper.FavoriteMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public void add(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BizException(ErrorCode.PARAM_ERROR, "商品不存在");
        }
        // 查找未删除的记录
        Favorite existing = favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .eq(Favorite::getDeleted, 0)
        );
        if (existing != null) {
            return; // 已在收藏中
        }
        // 查找软删除的记录，激活它
        Favorite deleted = favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .eq(Favorite::getDeleted, 1)
        );
        if (deleted != null) {
            deleted.setDeleted(0);
            favoriteMapper.updateById(deleted);
            log.info("恢复收藏，userId={}, productId={}", userId, productId);
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            log.info("添加收藏，userId={}, productId={}", userId, productId);
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        // 软删除
        favoriteMapper.update(
            null,
            new LambdaUpdateWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .eq(Favorite::getDeleted, 0)
                .set(Favorite::getDeleted, 1)
        );
        log.info("取消收藏（软删除），userId={}, productId={}", userId, productId);
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        Favorite fav = favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .eq(Favorite::getDeleted, 0)
        );
        return fav != null;
    }

    @Override
    public List<Product> list(Long userId) {
        List<Favorite> favs = favoriteMapper.selectList(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getDeleted, 0)
                .orderByDesc(Favorite::getCreateTime)
        );
        List<Product> result = new ArrayList<>();
        for (Favorite fav : favs) {
            Product product = productMapper.selectById(fav.getProductId());
            if (product != null && product.getDeleted() != 1) {
                result.add(product);
            }
        }
        return result;
    }

    @Override
    public Integer count(Long userId) {
        Long cnt = favoriteMapper.selectCount(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getDeleted, 0)
        );
        return cnt != null ? cnt.intValue() : 0;
    }
}
