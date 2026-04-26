package org.lin.fitnessworkout.repository;

import org.lin.fitnesscommon.entity.WorkoutPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanItemRepository extends JpaRepository<WorkoutPlanItem, Long> {

    List<WorkoutPlanItem> findByPlanIdOrderByDayOfWeekAsc(Long planId);

    void deleteByPlanId(Long planId);
}
