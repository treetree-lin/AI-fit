package org.lin.fitnessuser.service;
import org.lin.fitnessuser.dto.ProfileResponse;
import org.lin.fitnessuser.dto.RegisterRequest;
import org.lin.fitnesscommon.entity.User;
import org.lin.fitnesscommon.entity.UserProfile;
import org.lin.fitnessuser.repository.UserProfileRepository;
import org.lin.fitnessuser.repository.UserRepository;
import org.lin.fitnessuser.utils.JwtUtils;
import org.lin.fitnessuser.utils.PassWordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lin
 * @date 2026-03-17
 */

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 注册新用户,创建用户和用户画像
     *
     * @param request
     * @return
     */
    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PassWordUtils.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setProfileCompleted(false);


        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setAge(request.getAge());
        profile.setGender(UserProfile.Gender.fromChineseName(request.getGender()));
        profile.setHeight(request.getHeight().intValue());
        profile.setWeight(request.getWeight().intValue());
        profile.setBodyFat(request.getBodyFat());
        profile.setGoal(request.getGoal());
        profile.setLevel(request.getLevel());
        profile.setEquipment(request.getEquipment());
        profile.setInjuries(request.getInjuryHistory());
        profile.setAvatarUrl(request.getAvatarUrl());
        userProfileRepository.save(profile);

        return user.getId();
    }
    /**
     * 注册管理员用户
     *
     * @param request
     * @return
     */
    @Transactional
    @Override
    public Long registerAdmin(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PassWordUtils.encode(request.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setProfileCompleted(false);

        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setAge(request.getAge());
        profile.setGender(UserProfile.Gender.fromChineseName(request.getGender()));
        profile.setHeight(request.getHeight().intValue());
        profile.setWeight(request.getWeight().intValue());
        profile.setBodyFat(request.getBodyFat());
        profile.setGoal(request.getGoal());
        profile.setLevel(request.getLevel());
        profile.setEquipment(request.getEquipment());
        profile.setInjuries(request.getInjuryHistory());
        profile.setAvatarUrl(request.getAvatarUrl());

        userProfileRepository.save(profile);

        return user.getId();
    }

    /**
     * 登录用户
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!PassWordUtils.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtils.generateJWTToken(username);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());

        return result;
    }

    /**
     * 获取用户画像
     *
     * @param userId
     * @return
     */
    @Override
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户画像不存在"));

        ProfileResponse response = new ProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAge(profile.getAge());
        response.setGender(profile.getGender() != null ? profile.getGender().name() : null);
        response.setHeight(profile.getHeight() != null ? profile.getHeight().doubleValue() : null);
        response.setWeight(profile.getWeight() != null ? profile.getWeight().doubleValue() : null);
        response.setBodyFat(profile.getBodyFat());
        response.setGoal(profile.getGoal());
        response.setLevel(profile.getLevel());
        response.setEquipment(profile.getEquipment());
        response.setInjuryHistory(profile.getInjuries());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setBio(profile.getBio());
        response.setLocation(profile.getLocation());
        response.setExp(profile.getExp());
        response.setStreakDays(profile.getStreakDays());
        
        // 设置管理员专属字段
        response.setRole(user.getRole().name());
        response.setIsAdmin(user.getRole() == User.Role.ADMIN);
        response.setCreatedAt(user.getCreatedAt());
        response.setProfileCompleted(user.getProfileCompleted());
        
        return response;
    }

    /**
     * 更新用户画像
     *
     * @param userId
     * @param request
     */
    @Override
    @Transactional
    public void updateProfile(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户画像不存在"));

        if (request.getAge() != null) profile.setAge(request.getAge());
        if (request.getGender() != null) {
            profile.setGender(UserProfile.Gender.fromChineseName(request.getGender()));
        }
        if (request.getHeight() != null) profile.setHeight(request.getHeight().intValue());
        if (request.getWeight() != null) profile.setWeight(request.getWeight().intValue());
        if (request.getBodyFat() != null) profile.setBodyFat(request.getBodyFat());
        if (request.getGoal() != null) profile.setGoal(request.getGoal());
        if (request.getLevel() != null) profile.setLevel(request.getLevel());
        if (request.getEquipment() != null) profile.setEquipment(request.getEquipment());
        if (request.getInjuryHistory() != null) profile.setInjuries(request.getInjuryHistory());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getExp() != null) profile.setExp(request.getExp());
        userProfileRepository.save(profile);

        user.setProfileCompleted(true);
        userRepository.save(user);
    }
/**
     * 更新用户头像
     *
     * @param userId
     * @param avatarUrl
     */
    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        logger.info("开始更新用户{} 的头像", userId);
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("用户{} 的个人资料不存在", userId);
                    return new RuntimeException("用户画像不存在");
                });

        String oldAvatarUrl = profile.getAvatarUrl();
        profile.setAvatarUrl(avatarUrl);
        
        try {
            userProfileRepository.save(profile);
            logger.info("用户{} 头像更新成功: {} -> {}", userId, oldAvatarUrl, avatarUrl);
        } catch (Exception e) {
            logger.error("用户{} 头像保存失败: {}", userId, e.getMessage(), e);
            throw new RuntimeException("头像更新失败：" + e.getMessage());
        }
    }
    @Override
    public Long getCurrentUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private void checkAndMarkProfileCompleted(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        if (profile != null && isProfileComplete(profile)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setProfileCompleted(true);
            userRepository.save(user);
        }
    }

    private boolean isProfileComplete(UserProfile profile) {
        return profile.getAge() != null &&
               profile.getGender() != null &&
               profile.getHeight() != null &&
               profile.getWeight() != null &&
               profile.getGoal() != null &&
               profile.getLevel() != null;
    }



    public boolean updateUserProfileCompleted(Long userId, boolean completed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setProfileCompleted(completed);
        userRepository.save(user);
        return true;
    }
}

