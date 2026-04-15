package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.Cart;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.CartService;
import com.mall.vo.CartItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public void add(Long userId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BizException(ErrorCode.PARAM_ERROR, "商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new BizException(ErrorCode.PARAM_ERROR, "库存不足");
        }

        // 查找未删除的记录
        Cart cart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(Cart::getDeleted, 0)
        );
        if (cart != null) {
            // 已存在 -> 累加数量
            cart.setQuantity(cart.getQuantity() + quantity);
            cartMapper.updateById(cart);
            log.info("更新购物车数量，userId={}, productId={}, quantity={}",
                userId, productId, cart.getQuantity());
        } else {
            // 查找软删除的记录，激活它
            Cart deletedCart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getProductId, productId)
                    .eq(Cart::getDeleted, 1)
            );
            if (deletedCart != null) {
                deletedCart.setDeleted(0);
                deletedCart.setQuantity(quantity);
                cartMapper.updateById(deletedCart);
                log.info("恢复购物车商品，userId={}, productId={}", userId, productId);
            } else {
                // 全新添加
                cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cartMapper.insert(cart);
                log.info("新增购物车，userId={}, productId={}, quantity={}",
                    userId, productId, quantity);
            }
        }
    }

    @Override
    @Transactional
    public void remove(Long userId, Long productId) {
        // 软删除（不清物理删除，保持唯一键兼容）
        cartMapper.update(
            null,
            new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(Cart::getDeleted, 0)
                .set(Cart::getDeleted, 1)
        );
        log.info("移除购物车商品（软删除），userId={}, productId={}", userId, productId);
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            remove(userId, productId);
            return;
        }
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            throw new BizException(ErrorCode.PARAM_ERROR, "库存不足");
        }
        Cart cart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(Cart::getDeleted, 0)
        );
        if (cart != null) {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        // 批量软删除
        cartMapper.update(
            null,
            new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
                .set(Cart::getDeleted, 1)
        );
        log.info("清空购物车，userId={}", userId);
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        List<Cart> carts = cartMapper.selectList(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
        );
        List<CartItemVO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null && product.getDeleted() != 1) {
                result.add(new CartItemVO(cart.getId(), product, cart.getQuantity()));
            }
        }
        return result;
    }

    @Override
    public Integer count(Long userId) {
        Long cnt = cartMapper.selectCount(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
        );
        return cnt != null ? cnt.intValue() : 0;
    }
}
