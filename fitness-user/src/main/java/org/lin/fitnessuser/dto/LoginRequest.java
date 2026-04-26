package org.lin.fitnessuser.dto;
import lombok.Data;
/**
 * @author lin
 * @date 2026-03-17
 */

@Data
public class LoginRequest {
    private String username;
    private String password;
}
