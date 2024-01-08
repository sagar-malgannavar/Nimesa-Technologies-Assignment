package com.assignment.nimesa.discover.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.assignment.nimesa.discover.entity.JobStatus;
import com.assignment.nimesa.discover.repository.JobRepository;
import com.assignment.nimesa.discover.repository.S3Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.assignment.nimesa.discover.entity.EC2Instance;
import com.assignment.nimesa.discover.entity.Job;
import com.assignment.nimesa.discover.entity.S3Bucket;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DiscoveryService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private S3Repository s3Repository;

    @Autowired
    private AWSService awsService; // Your AWS service for interacting with EC2 and S3

    private ExecutorService executorService = Executors.newFixedThreadPool(2); // Two threads for EC2 and S3 tasks

    public String discoverServices(List<String> services) {

        String jobId = UUID.randomUUID().toString();

        CompletableFuture<Void> ec2Discovery = CompletableFuture.runAsync(() -> {
            List<String> ec2Instances = awsService.discoverEC2Instances("Mumbai");
            saveEC2Instances(jobId, ec2Instances); // Method to save EC2 instances to the database
        }, executorService);

        CompletableFuture<Void> s3Discovery = CompletableFuture.runAsync(() -> {
            List<S3Bucket> s3Buckets = awsService.discoverS3Buckets("Mumbai"); // Replace with your S3 discovery logic
            saveS3Buckets(jobId, s3Buckets); // Method to save S3 buckets to the database
        }, executorService);

        CompletableFuture.allOf(ec2Discovery, s3Discovery)
                .thenRun(() -> updateJobStatus(jobId)); // Update job status once both tasks are completed

        return jobId;
    }

    private void saveEC2Instances(String jobId, List<String> ec2Instances) {
        Job job = jobRepository.findByJobId(jobId).get();
        jobRepository.save(job);

    }

    private void saveS3Buckets(String jobId, List<S3Bucket> s3Buckets) {
        // Save S3 buckets to the database using jobRepository.save()
    }

    private void updateJobStatus(String jobId) {
        // Update job status in the database as completed
        Job job = jobRepository.findByJobId(jobId).orElse(null);
        if (job != null) {
            job.setJobStatus(JobStatus.SUCCESS); // Update with appropriate status handling
            jobRepository.save(job);
        }
    }

    public JobStatus getJobResult(String jobId) {
        Optional<Job> jobOptional = jobRepository.findByJobId(jobId);
        return jobOptional.map(Job::getJobStatus).orElse(null);
    }

    public List<String> discoverEC2Instances() {
        return Collections.singletonList("i-12345678"); // To be replaced with actual EC2 instances
    }

    public List<String> discoverS3Buckets() {
        // Logic to retrieve S3 buckets
        return Arrays.asList("bucket1", "bucket2"); // Replace with actual bucket names
    }

    @Async
    public String discoverAndPersistS3BucketObjects(String bucketName) {
        String jobId = UUID.randomUUID().toString();

        // Retrieve all file names in the S3 bucket
        List<String> fileNames = listAllS3Objects(bucketName);

        // Persist the file names in the database
        persistFileNames(jobId, fileNames);

        return jobId;
    }

    private List<String> listAllS3Objects(String bucketName) {
        List<String> fileNames = new ArrayList<>();

        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName);

        ListObjectsV2Result result;

        do {
            result = s3Client.listObjectsV2(listObjectsRequest);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                fileNames.add(objectSummary.getKey());
            }

            listObjectsRequest.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return fileNames;
    }

    private void persistFileNames(String jobId, List<String> fileNames) {
        // Persist the file names in the database using the repository
        for (String fileName : fileNames) {
            s3Repository.saveFileName(jobId, fileName);
        }
    }

    public int getS3BucketObjectCount(String bucketName) {
        int count = s3Repository.getFileCountForBucket(bucketName); // Replace with actual database call
        return count;
    }


    public List<String> getS3BucketObjectLike(String bucketName, String pattern) {
        List<String> matchingFiles = s3Repository.getMatchingFilesForBucket(bucketName, pattern);
        return matchingFiles;
    }
}
