package org.lin.fitnessworkout.mq.consumer;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnessworkout.config.RabbitMQConfig;
import org.lin.fitnessworkout.mq.event.FavoriteEvent;
import org.lin.fitnessworkout.mq.event.CommentEvent;
import org.lin.fitnessworkout.service.WorkoutService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatsEventListener {

    @Autowired
    private WorkoutService workoutService;

    @RabbitListener(queues = RabbitMQConfig.FAVORITE_QUEUE)
    public void handleFavoriteEvent(FavoriteEvent event) {
        if ("ADD".equals(event.getAction())) {
            workoutService.incrementFavoriteCount(event.getWorkoutId());
        } else if ("REMOVE".equals(event.getAction())) {
            workoutService.decrementFavoriteCount(event.getWorkoutId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleCommentEvent(CommentEvent event) {
        if ("ADD".equals(event.getAction())) {
            workoutService.incrementCommentCount(event.getWorkoutId());
        } else if ("REMOVE".equals(event.getAction())) {
            workoutService.decrementCommentCount(event.getWorkoutId());
        }
    }
}
