package com.mcu.repository

import com.mcu.model.History
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class HistoryRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector
    fun save(item: History): History? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}