package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.mcu.model.DynamoUser
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoUserRepository {
    @Autowired
    private lateinit var awsConnector: AwsConnector

    fun save(item : DynamoUser): DynamoUser? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun findUserByUserId(userId : String) : DynamoUser? {
        val item= DynamoUser(userId)
        return try {
            awsConnector.getDynamoDBMapper().load(item)
        } catch (e : Exception) {
            null
        }
    }

    fun findUserByNickname(nickname : String) : DynamoUser? {
        val dynamoUser = DynamoUser().also { it.nickname = nickname }
        val queryExpression = DynamoDBQueryExpression<DynamoUser>()
        queryExpression.indexName = "User-nickname"
        queryExpression.hashKeyValues = dynamoUser
        queryExpression.isConsistentRead = false
        val result = awsConnector.getDynamoDBMapper().query(DynamoUser::class.java, queryExpression)
        return if(result.size == 0) {
            null
        }
        else {
            result.first()
        }
    }

    fun findUserByEmail(email : String) : DynamoUser? {
        val dynamoUser = DynamoUser().also { it.email = email }
        val queryExpression = DynamoDBQueryExpression<DynamoUser>()
        queryExpression.indexName = "User-email"
        queryExpression.hashKeyValues = dynamoUser
        queryExpression.isConsistentRead = false
        val result = awsConnector.getDynamoDBMapper().query(DynamoUser::class.java, queryExpression)
        return if(result.size == 0) {
            null
        }
        else {
            result.first()
        }
    }

}