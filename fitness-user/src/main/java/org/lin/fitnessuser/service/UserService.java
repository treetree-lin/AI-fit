package org.lin.fitnessuser.service;

import org.lin.fitnessuser.dto.ProfileResponse;
import org.lin.fitnessuser.dto.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author lin
 * @date 2026-03-17
 */

public interface UserService {
    Long register(RegisterRequest request);

    @Transactional
    Long registerAdmin(RegisterRequest request);

    Map<String, Object> login(String username, String password);
    ProfileResponse getProfile(Long userId);
    void updateProfile(Long userId, RegisterRequest request);
    Long getCurrentUserIdByUsername(String username);
    boolean updateUserProfileCompleted(Long userId, boolean completed);
    void updateAvatar(Long userId, String avatarUrl);

}
