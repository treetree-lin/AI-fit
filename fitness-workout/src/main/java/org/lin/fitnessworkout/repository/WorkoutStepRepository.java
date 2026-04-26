package org.lin.fitnessworkout.repository;
import org.lin.fitnesscommon.entity.WorkoutStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author lin
 * @date 2026-03-24
 */



@Repository
public interface WorkoutStepRepository extends JpaRepository<WorkoutStep, Long> {

    List<WorkoutStep> findByWorkoutIdOrderByStepOrder(Long workoutId);

    void deleteByWorkoutId(Long workoutId);
}
