package org.lin.fitnessrecord.repository;
import org.lin.fitnesscommon.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
/**
 * @author lin
 * @date 2026-03-24
 */



@Repository
public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {

    List<WorkoutRecord> findByUserIdOrderByRecordDateDesc(Long userId);

    List<WorkoutRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    List<WorkoutRecord> findByUserId(Long userId);
}
