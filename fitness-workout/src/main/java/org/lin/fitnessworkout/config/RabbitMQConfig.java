package org.lin.fitnessworkout.config;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // 交换机
    public static final String FAVORITE_EXCHANGE = "workout.favorite.exchange";
    public static final String COMMENT_EXCHANGE = "workout.comment.exchange";
    // 队列
    public static final String FAVORITE_QUEUE = "workout.favorite.queue";
    public static final String COMMENT_QUEUE = "workout.comment.queue";
    // 路由键：交换机根据根据Routing Key决定把消息发送到哪个队列。
    public static final String FAVORITE_ROUTING_KEY = "workout.favorite.routing";
    public static final String COMMENT_ROUTING_KEY = "workout.comment.routing";

    @Bean
    public TopicExchange favoriteExchange() {
        return new TopicExchange(FAVORITE_EXCHANGE);
    }

    @Bean
    public TopicExchange commentExchange() {
        return new TopicExchange(COMMENT_EXCHANGE);
    }

    @Bean
    public Queue favoriteQueue() {
        return QueueBuilder.durable(FAVORITE_QUEUE).build();
    }

    @Bean
    public Queue commentQueue() {
        return QueueBuilder.durable(COMMENT_QUEUE).build();
    }

    @Bean
    public Binding favoriteBinding(Queue favoriteQueue, TopicExchange favoriteExchange) {
        return BindingBuilder.bind(favoriteQueue).to(favoriteExchange).with(FAVORITE_ROUTING_KEY);
    }

    @Bean
    public Binding commentBinding(Queue commentQueue, TopicExchange commentExchange) {
        return BindingBuilder.bind(commentQueue).to(commentExchange).with(COMMENT_ROUTING_KEY);// 绑定路由键
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
