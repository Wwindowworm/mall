package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.*;
import com.mall.mapper.*;
import com.mall.service.OrderService;
import com.mall.vo.OrderDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public Order createFromCart(Long userId, Long addressId, String remark) {
        List<Cart> carts = cartMapper.selectList(
            new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)
        );
        if (carts == null || carts.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "购物车为空");
        }

        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "收货地址无效");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setRemark(remark);
        order.setStatus(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (Cart cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getDeleted() == 1) continue;
            if (product.getStock() < cart.getQuantity()) {
                throw new BizException(ErrorCode.PARAM_ERROR,
                    "商品「" + product.getName() + "」库存不足");
            }

            // 扣库存
            productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, product.getId())
                    .set(Product::getStock, product.getStock() - cart.getQuantity())
                    .set(Product::getSalesCount, product.getSalesCount() + cart.getQuantity())
            );

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setOrderId(0L);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        // 运费：满99免运费，否则6元
        BigDecimal freight = totalAmount.compareTo(new BigDecimal("99")) >= 0
            ? BigDecimal.ZERO : new BigDecimal("6.00");
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(freight);
        order.setPayAmount(totalAmount.add(freight));

        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 清空购物车
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        log.info("创建订单，orderNo={}, userId={}, amount={}",
            order.getOrderNo(), userId, order.getPayAmount());
        return order;
    }

    @Override
    @Transactional
    public Order createDirect(Long userId, Long productId, Integer quantity, Long addressId, String remark) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BizException(ErrorCode.PARAM_ERROR, "商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new BizException(ErrorCode.PARAM_ERROR, "库存不足");
        }

        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "收货地址无效");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setRemark(remark);
        order.setStatus(0);

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal freight = subtotal.compareTo(new BigDecimal("99")) >= 0
            ? BigDecimal.ZERO : new BigDecimal("6.00");

        order.setTotalAmount(subtotal);
        order.setFreightAmount(freight);
        order.setPayAmount(subtotal.add(freight));

        productMapper.update(null,
            new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .set(Product::getStock, product.getStock() - quantity)
                .set(Product::getSalesCount, product.getSalesCount() + quantity)
        );

        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImage());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setSubtotal(subtotal);
        orderItemMapper.insert(item);

        log.info("直接下单，orderNo={}, userId={}, productId={}, qty={}",
            order.getOrderNo(), userId, productId, quantity);
        return order;
    }

    @Override
    public OrderDetailVO getDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrder(order);
        vo.setItems(items);
        return vo;
    }

    @Override
    public List<Order> list(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .eq(Order::getUserId, userId)
            .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return orderMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "只有待支付订单可以取消");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, item.getProductId())
                        .set(Product::getStock, product.getStock() + item.getQuantity())
                        .set(Product::getSalesCount,
                            Math.max(0, product.getSalesCount() - item.getQuantity()))
                );
            }
        }

        order.setStatus(4);
        orderMapper.updateById(order);
        log.info("取消订单，orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional
    public void pay(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单状态不可支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单支付成功，orderNo={}", orderNo);
    }

    @Override
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BizException(ErrorCode.PARAM_ERROR, "只有已发货订单可以确认收货");
        }
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("确认收货，orderNo={}", order.getOrderNo());
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
