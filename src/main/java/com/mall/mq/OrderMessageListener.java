package com.mall.mq;

import com.mall.service.OrderMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.mall.config.RabbitMQConfig;
import com.mall.entity.SeckillOrder;

@Component
public class OrderMessageListener {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageListener.class);

    @RabbitListener(queues = RabbitMQConfig.SEC_KILL_QUEUE)
    public void handleSeckillOrder(SeckillOrder order) {
        log.info("【秒杀消息队列】收到订单：orderNo={}, userId={}, activityId={}, seckillPrice={}",
                order.getOrderNo(), order.getUserId(), order.getActivityId(), order.getSeckillPrice());
        // TODO: 后续可在这里处理库存同步、库存预警、发邮件通知等
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrder(OrderMessageService.OrderMessage message) {
        log.info("【普通订单队列】收到消息：orderNo={}, userId={}, productId={}, timestamp={}",
                message.getOrderNo(), message.getUserId(), message.getProductId(), message.getTimestamp());
        // TODO: 后续可在这里处理订单确认、发送通知等
    }
}