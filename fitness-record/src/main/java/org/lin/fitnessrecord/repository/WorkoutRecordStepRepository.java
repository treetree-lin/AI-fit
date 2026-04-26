package org.lin.fitnessrecord.repository;

/**
 * @author lin
 * @date 2026-03-24
 */

import org.lin.fitnesscommon.entity.WorkoutRecordStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRecordStepRepository extends JpaRepository<WorkoutRecordStep, Long> {

    List<WorkoutRecordStep> findByRecordId(Long recordId);

    void deleteByRecordId(Long recordId);
}
