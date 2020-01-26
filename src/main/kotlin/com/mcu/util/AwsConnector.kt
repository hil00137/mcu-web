package com.mcu.util

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AwsConnector {

    @Value("\${spring.profiles}")
    private lateinit var profile : String

    @Value("\${aws.access.key.id}")
    private lateinit var accessKey : String
    @Value("\${aws.secret.key}")
    private lateinit var secretKey : String

    private var amazonEC2 : AmazonEC2? = null

    private var amazonDynamoDB : AmazonDynamoDB? = null

    private var dynamoDBMapper : DynamoDBMapper? = null

    private var amazonS3 : AmazonS3? = null

    /**
     * EC2
     */
    fun getEC2Connection() : AmazonEC2 {
        return if(amazonEC2 != null) {
            amazonEC2 as AmazonEC2
        } else {
            amazonEC2 = AmazonEC2ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))).withRegion(Regions.AP_NORTHEAST_2).build()
            amazonEC2 as AmazonEC2
        }
    }

    /**
     * DynamoDB
     */
    fun getDynamoDBConnection(): AmazonDynamoDB {
        return if(amazonDynamoDB == null) {
            if (profile == "local") {
                amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))).withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "ap-northeast-2")).build()
                amazonDynamoDB!!
            } else {
                amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))).withRegion(Regions.AP_NORTHEAST_2).build()
                amazonDynamoDB!!
            }
        } else {
            amazonDynamoDB!!
        }

    }

    fun getDynamoDBMapper() : DynamoDBMapper {
        return if(dynamoDBMapper == null) {
            DynamoDBMapper(this.getDynamoDBConnection(), DynamoDBMapperConfig.DEFAULT)
        } else {
            dynamoDBMapper!!
        }
    }

    /**
     * S3
     */
    fun getS3Connection() : AmazonS3 {
        return if(amazonS3 != null) {
            amazonS3 as AmazonS3
        } else {
            amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))).withRegion(Regions.AP_NORTHEAST_2).build()
            amazonS3 as AmazonS3
        }
    }
}