package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList
import com.mcu.model.DynamoServer
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoServerRepository {

    @Autowired
    private lateinit var awsConnector: AwsConnector

    fun findByName(name : String) : DynamoServer? {
        val item = DynamoServer(name)
        return try {
            awsConnector.getDynamoDBMapper().load(item)
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }

    fun findAll(): PaginatedScanList<DynamoServer> {
        return awsConnector.getDynamoDBMapper().scan(DynamoServer::class.java, DynamoDBScanExpression())
    }

    fun save(item : DynamoServer) : DynamoServer {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}