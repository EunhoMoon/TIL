package com.motivank.consumer.consumer;

import com.motivank.consumer.domain.Coupon;
import com.motivank.consumer.domain.FailedEvent;
import com.motivank.consumer.repository.CouponRepository;
import com.motivank.consumer.repository.FailedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;

    private final FailedEventRepository failedEventRepository;

    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    public CouponCreatedConsumer(
            CouponRepository couponRepository,
            FailedEventRepository failedEventRepository
    ) {
        this.couponRepository = couponRepository;
        this.failedEventRepository = failedEventRepository;
    }

    @KafkaListener(topics = "coupon-create", groupId = "group_1")
    public void listen(Long userId) {
        try {
            couponRepository.save(new Coupon(userId));
        } catch (Exception e) {
            logger.error("error", e);
            failedEventRepository.save(new FailedEvent(userId));
        }
    }

}
