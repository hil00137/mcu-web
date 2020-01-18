package com.mcu.repository

import com.mcu.model.DynamoHistory
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoHistoryRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector
    fun save(item: DynamoHistory): DynamoHistory? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}