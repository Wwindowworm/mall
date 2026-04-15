package com.mall.service;

import com.mall.config.RabbitMQConfig;
import com.mall.entity.SeckillOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageService {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀订单创建消息
     */
    public void sendSeckillOrderMessage(SeckillOrder order) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SEC_KILL_EXCHANGE,
                    RabbitMQConfig.SEC_KILL_ROUTING_KEY,
                    order
            );
            log.info("秒杀订单消息已发送：orderNo={}, activityId={}", order.getOrderNo(), order.getActivityId());
        } catch (Exception e) {
            log.error("发送秒杀订单消息失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送普通订单创建消息
     */
    public void sendOrderMessage(Long userId, Long productId, String orderNo) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    new OrderMessage(userId, productId, orderNo)
            );
            log.info("普通订单消息已发送：orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("发送普通订单消息失败：{}", e.getMessage(), e);
        }
    }

    public static class OrderMessage {
        private Long userId;
        private Long productId;
        private String orderNo;
        private long timestamp;

        public OrderMessage() {}
        public OrderMessage(Long userId, Long productId, String orderNo) {
            this.userId = userId;
            this.productId = productId;
            this.orderNo = orderNo;
            this.timestamp = System.currentTimeMillis();
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}