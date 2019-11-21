package com.mcu.util

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AwsConnector {

    @Value("\${aws.access.key.id}")
    private lateinit var accessKey : String
    @Value("\${aws.secret.key}")
    private lateinit var secretKey : String

    private var amazonEC2 : AmazonEC2? = null

    fun getConnection() : AmazonEC2 {
        return if(amazonEC2 != null) {
            amazonEC2 as AmazonEC2
        } else {
            amazonEC2 = AmazonEC2ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))).withRegion(Regions.AP_NORTHEAST_2).build()
            amazonEC2 as AmazonEC2
        }
    }
}