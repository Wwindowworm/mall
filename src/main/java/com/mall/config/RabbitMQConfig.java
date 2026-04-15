package com.mall.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "mall.order.exchange";
    public static final String ORDER_QUEUE = "mall.order.queue";
    public static final String ORDER_DELAYED_QUEUE = "mall.order.delayed.queue";
    public static final String ORDER_ROUTING_KEY = "mall.order.create";
    public static final String ORDER_DELAYED_ROUTING_KEY = "mall.order.delayed";

    public static final String SEC_KILL_EXCHANGE = "mall.seckill.exchange";
    public static final String SEC_KILL_QUEUE = "mall.seckill.queue";
    public static final String SEC_KILL_ROUTING_KEY = "mall.seckill.create";

    // ========== 商品下单消息 ==========

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_ROUTING_KEY + ".dlq")
                .build();
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }

    // ========== 秒杀消息 ==========

    @Bean
    public TopicExchange secKillExchange() {
        return new TopicExchange(SEC_KILL_EXCHANGE, true, false);
    }

    @Bean
    public Queue secKillQueue() {
        return QueueBuilder.durable(SEC_KILL_QUEUE).build();
    }

    @Bean
    public Binding secKillBinding(Queue secKillQueue, TopicExchange secKillExchange) {
        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with(SEC_KILL_ROUTING_KEY);
    }

    // ========== 全局限流队列 ==========

    public static final String RATE_LIMIT_QUEUE = "mall.rate.limit.queue";

    @Bean
    public Queue rateLimitQueue() {
        return QueueBuilder.durable(RATE_LIMIT_QUEUE).build();
    }

    // ========== 消息转换器 ==========

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}