package org.lin.fitnessworkout.mq.producer;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnessworkout.config.RabbitMQConfig;
import org.lin.fitnessworkout.mq.event.FavoriteEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatsEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendFavoriteEvent(FavoriteEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FAVORITE_EXCHANGE,
            RabbitMQConfig.FAVORITE_ROUTING_KEY,
            event
        );
    }

    public void sendCommentEvent(org.lin.fitnessworkout.mq.event.CommentEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMMENT_EXCHANGE,
            RabbitMQConfig.COMMENT_ROUTING_KEY,
            event
        );
    }
}
