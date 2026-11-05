package com.macro.mall.mq;

import com.macro.mall.config.RabbitConfig;
import com.macro.mall.service.PortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutConsumer {
    private final PortalOrderService portalOrderService;

    public OrderTimeoutConsumer(PortalOrderService portalOrderService) {
        this.portalOrderService = portalOrderService;
    }

    /**
     * 消费端幂等：仅当订单仍处于「待付款」且条件更新成功时才执行取消与资源回滚；
     * 重复投递、已支付或已取消订单都会被安全忽略。
     */
    @RabbitListener(queues = RabbitConfig.CANCEL_QUEUE)
    public void handle(OrderTimeoutMessage message) {
        log.info("收到订单超时消息 orderId={}, orderSn={}", message.getOrderId(), message.getOrderSn());
        try {
            portalOrderService.timeoutCancel(message.getOrderId());
        } catch (Exception e) {
            log.error("处理订单超时取消失败 orderId={}", message.getOrderId(), e);
            throw e;
        }
    }
}
