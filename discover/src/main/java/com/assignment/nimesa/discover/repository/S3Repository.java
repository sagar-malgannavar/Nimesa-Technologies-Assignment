package com.assignment.nimesa.discover.repository;

import com.assignment.nimesa.discover.entity.S3File;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface S3Repository extends JpaRepository<S3File, Long> {

    @Modifying
    @Query("INSERT INTO S3File(jobId, fileName) VALUES (:jobId, :fileName)")
    void saveFileName(@Param("jobId") String jobId, @Param("fileName") String fileName);

    @Query("SELECT COUNT(sf) FROM S3File sf WHERE sf.bucketName = :bucketName")
    int getFileCountForBucket(@Param("bucketName") String bucketName);

    @Query("SELECT sf.fileName FROM S3File sf WHERE sf.bucketName = :bucketName AND sf.fileName LIKE %:pattern%")
    List<String> getMatchingFilesForBucket(@Param("bucketName") String bucketName, @Param("pattern") String pattern);


}
