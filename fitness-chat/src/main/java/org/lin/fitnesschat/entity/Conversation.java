package org.lin.fitnesschat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import org.lin.fitnesscommon.entity.User;
/**
 * @author lin
 * @date 2026-04-17
 */

@Data
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 对话记录唯一标识

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 关联用户

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question; // 用户提问内容

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer; // 系统回答内容

    @CreationTimestamp
    private LocalDateTime timestamp; // 对话时间戳
}