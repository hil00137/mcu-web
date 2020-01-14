package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList
import com.mcu.model.Server
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class ServerRepository {

    @Autowired
    private lateinit var awsConnector: AwsConnector

    fun findByName(name : String) : Server? {
        val item = Server(name)
        return try {
            awsConnector.getDynamoDBMapper().load(item)
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }

    fun findAll(): PaginatedScanList<Server> {
        return awsConnector.getDynamoDBMapper().scan(Server::class.java, DynamoDBScanExpression())
    }

    fun save(item : Server) : Server {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}