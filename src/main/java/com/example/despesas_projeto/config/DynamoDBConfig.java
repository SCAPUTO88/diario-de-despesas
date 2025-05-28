package com.example.despesas_projeto.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableDynamoDBRepositories(
        basePackages = "com.example.despesas_projeto.repository",
        includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.REGEX,
                pattern = "com\\.example\\.despesas_projeto\\.repository\\..*"
        )
)
public class DynamoDBConfig {

    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDbEndpoint;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.access.key}")
    private String awsAccessKey;

    @Value("${aws.secret.key}")
    private String awsSecretKey;

    @Bean
    @Primary
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, awsRegion))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                .build();
    }

    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }

    @Bean(name = "dynamoDBLocal")
    public AmazonDynamoDB amazonDynamoDBLocal() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                "http://localhost:8000", "sa-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("fakeMyKeyId", "fakeSecretAccessKey")))
                .build();
    }

    @Bean(name = "dynamoDBMapperLocal")
    public DynamoDBMapper dynamoDBMapperLocal() {
        return new DynamoDBMapper(amazonDynamoDBLocal());
    }
}