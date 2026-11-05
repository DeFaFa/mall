package com.macro.mall.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ TTL + 死信队列：
 * 下单后发送一条带过期时间（默认 30 分钟）的消息到 delay.queue，
 * 过期后由 broker 投递到死信交换机，再由订单取消消费者处理。
 */
@Configuration
public class RabbitConfig {
    public static final String DELAY_EXCHANGE = "mall.order.delay.exchange";
    public static final String DELAY_QUEUE = "mall.order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "mall.order.delay";

    public static final String DLX_EXCHANGE = "mall.order.dlx.exchange";
    public static final String CANCEL_QUEUE = "mall.order.cancel.queue";
    public static final String CANCEL_ROUTING_KEY = "mall.order.cancel";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(DELAY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", CANCEL_ROUTING_KEY);
        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue orderCancelQueue() {
        return new Queue(CANCEL_QUEUE, true);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with(DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderDlxExchange()).with(CANCEL_ROUTING_KEY);
    }
}
