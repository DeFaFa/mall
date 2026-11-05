package com.macro.mall.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.config.RabbitConfig;
import com.macro.mall.entity.OrderSetting;
import com.macro.mall.mapper.OrderSettingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutSender {
    private final AmqpTemplate amqpTemplate;
    private final OrderSettingMapper orderSettingMapper;
    private final long defaultTimeoutMinutes;

    public OrderTimeoutSender(AmqpTemplate amqpTemplate,
                              OrderSettingMapper orderSettingMapper,
                              @Value("${mall.order.timeout-minutes:30}") long timeoutMinutes) {
        this.amqpTemplate = amqpTemplate;
        this.orderSettingMapper = orderSettingMapper;
        this.defaultTimeoutMinutes = timeoutMinutes;
    }

    /**
     * 发送延迟消息。TTL 通过消息属性设置，过期后进入死信队列。
     * 优先使用订单设置中的普通订单超时秒数（默认 1800 秒 = 30 分钟）。
     */
    public void send(Long orderId, String orderSn) {
        OrderTimeoutMessage message = new OrderTimeoutMessage(orderId, orderSn);
        OrderSetting setting = orderSettingMapper.selectOne(new LambdaQueryWrapper<OrderSetting>().last("limit 1"));
        long timeoutSeconds = setting == null || setting.getNormalOrderOvertime() == null
                ? defaultTimeoutMinutes * 60
                : setting.getNormalOrderOvertime();
        long ttl = timeoutSeconds * 1000L;
        try {
            amqpTemplate.convertAndSend(RabbitConfig.DELAY_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, message, msg -> {
                msg.getMessageProperties().setExpiration(String.valueOf(ttl));
                return msg;
            });
            log.info("订单超时延迟消息已发送 orderId={}, ttl={}ms", orderId, ttl);
        } catch (Exception e) {
            // MQ 不可用时不影响主流程，订单仍可通过后台或用户手动取消
            log.error("订单超时延迟消息发送失败 orderId={}", orderId, e);
        }
    }
}
