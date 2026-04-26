package org.lin.fitnesschat.repository;

import org.lin.fitnesschat.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author lin
 * @date 2026-04-17
 */

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, String> {
    Optional<FileUpload> findByFileMd5(String fileMd5);

    Optional<FileUpload> findByFileMd5AndUserId(String fileMd5, String userId);

    Optional<FileUpload> findByFileNameAndIsPublicTrue(String fileName);

    long countByFileMd5(String fileMd5);

    void deleteByFileMd5(String fileMd5);

    void deleteByFileMd5AndUserId(String fileMd5, String userId);

    /**
     * 查询用户自己的文件和公开文件
     */
    List<FileUpload> findByUserIdOrIsPublicTrue(String userId);

    /**
     * 查询用户可访问的所有文件（考虑层级标签权限）
     * 包括：1. 用户自己上传的文件
     *      2. 公开的文件
     *      3. 用户所属组织的文件（包含层级关系）
     *
     * @param userId 用户ID
     * @return 用户可访问的文件列表
     */
    @Query("SELECT f FROM FileUpload f WHERE f.userId = :userId OR f.isPublic = true ")
    List<FileUpload> findAccessibleFilesWithTags(@Param("userId") String userId);

    /**
     * 查询用户可访问的所有文件（原始方法，保留向后兼容性）
     *
     * @param userId 用户ID
     * @return 用户可访问的文件列表
     */
    @Query("SELECT f FROM FileUpload f WHERE f.userId = :userId OR f.isPublic = true ")
    List<FileUpload> findAccessibleFiles(@Param("userId") String userId);

    /**
     * 查询用户自己上传的所有文件
     *
     * @param userId 用户ID
     * @return 用户上传的文件列表
     */
    List<FileUpload> findByUserId(String userId);

    List<FileUpload> findByFileMd5In(List<String> md5List);
}
