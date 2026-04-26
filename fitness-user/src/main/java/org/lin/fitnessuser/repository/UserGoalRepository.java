package org.lin.fitnessuser.repository;
import org.lin.fitnesscommon.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author lin
 * @date 2026-03-24
 */



@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    List<UserGoal> findByUserId(Long userId);

    List<UserGoal> findByUserIdAndStatus(Long userId, Integer status);
}

