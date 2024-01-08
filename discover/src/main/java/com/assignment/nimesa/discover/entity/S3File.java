package com.assignment.nimesa.discover.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "s3_files")
public class S3File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "file_name")
    private String fileName;
}

