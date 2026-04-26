package org.lin.fitnesschat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lin
 * @date 2026-04-17
 */
@Data
@AllArgsConstructor
public class Message {
    private String role;
    private String content;
}