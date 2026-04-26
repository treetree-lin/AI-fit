package org.lin.fitnesschat.repository;

import org.lin.fitnesschat.entity.ChunkInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author lin
 * @date 2026-04-17
 */
public interface ChunkInfoRepository extends JpaRepository<ChunkInfo, Long> {
    List<ChunkInfo> findByFileMd5OrderByChunkIndexAsc(String fileMd5);
}