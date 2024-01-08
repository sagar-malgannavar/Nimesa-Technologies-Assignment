package com.assignment.nimesa.discover.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.assignment.nimesa.discover.entity.EC2Instance;
import com.assignment.nimesa.discover.entity.S3Bucket;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Service
public class AWSService {

    @Value("${aws.region}")
    private String awsRegion; // Autowire the AWS region from properties file or environment variables

    @Value("${aws.accessKeyId}")
    private String accessKeyId; // Autowire AWS access key ID

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey; // Autowire AWS secret access key

    private AmazonEC2 ec2Client; // AWS EC2 client
    private AmazonS3 s3Client; // AWS S3 client

    @PostConstruct
    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

        // Initialize AWS clients
        ec2Client = AmazonEC2ClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public List<String> discoverEC2Instances(String region) {
        // Use ec2Client to discover EC2 instances in the specified region
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        // If you want to filter by region, set the region in the request
        if (region != null && !region.isEmpty()) {
            request.setRegion(region);
        }

        List<String> instanceIds = new ArrayList<>();

        DescribeInstancesResult response = ec2Client.describeInstances(request);

        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                instanceIds.add(instance.getInstanceId());
            }
        }

        return instanceIds;

    }

    public List<S3Bucket> discoverS3Buckets(String region) {
        List<S3Bucket> buckets = new ArrayList<>();
        // Replace with actual buckets
        return buckets;
    }
}
