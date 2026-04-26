package org.lin.fitnessuser.repository;

/**
 * @author lin
 * @date 2026-03-17
 */
import org.lin.fitnesscommon.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 更新连续打卡天数和最后打卡日期
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserProfile u SET u.streakDays = :streakDays, u.lastCheckInDate = :lastCheckInDate WHERE u.userId = :userId")
    void updateStreakDays(@Param("userId") Long userId, 
                          @Param("streakDays") Integer streakDays,
                          @Param("lastCheckInDate") LocalDate lastCheckInDate);
}

