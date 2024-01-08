package com.assignment.nimesa.discover.controller;

import com.assignment.nimesa.discover.entity.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.assignment.nimesa.discover.service.DiscoveryService;

import java.util.List;

@RestController
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @PostMapping("/services")
    public String discoverServices(@RequestBody List<String> services) {
        return discoveryService.discoverServices(services);
    }

    @GetMapping("/job/{jobId}")
    public JobStatus getJobResult(@PathVariable String jobId) {
        return discoveryService.getJobResult(jobId);
    }

    @GetMapping("/discoveryResult/{service}")
    public ResponseEntity<?> getDiscoveryResult(@PathVariable String service) {
        if ("S3".equalsIgnoreCase(service)) {
            List<String> s3Buckets = discoveryService.discoverS3Buckets(); // Your method to retrieve S3 buckets
            return ResponseEntity.ok().body(s3Buckets);
        } else if ("EC2".equalsIgnoreCase(service)) {
            List<String> ec2Instances = discoveryService.discoverEC2Instances(); // Your method to retrieve EC2 instances
            return ResponseEntity.ok().body(ec2Instances);
        } else {
            return ResponseEntity.badRequest().body("Invalid service name provided");
        }
    }

    @GetMapping("/s3BucketObjects/{bucketName}")
    public ResponseEntity<String> getS3BucketObjects(@PathVariable String bucketName) {
        String jobId = discoveryService.discoverAndPersistS3BucketObjects(bucketName);
        return ResponseEntity.ok().body(jobId);
    }

    @GetMapping("/s3BucketObjectCount/{bucketName}")
    public ResponseEntity<Integer> getS3BucketObjectCount(@PathVariable String bucketName) {
        int count = discoveryService.getS3BucketObjectCount(bucketName);
        return ResponseEntity.ok().body(count);
    }

    @GetMapping("/s3BucketObjectLike/{bucketName}/{pattern}")
    public ResponseEntity<List<String>> getS3BucketObjectLike(
            @PathVariable String bucketName, @PathVariable String pattern) {
        List<String> matchingFiles = discoveryService.getS3BucketObjectLike(bucketName, pattern);
        return ResponseEntity.ok().body(matchingFiles);
    }


}
