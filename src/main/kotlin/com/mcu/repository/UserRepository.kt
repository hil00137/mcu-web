package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.mcu.model.User
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Repository

@Repository
class UserRepository {
    @Autowired
    private lateinit var awsConnector: AwsConnector

    @CacheEvict(value = ["userCache"], allEntries = true)
    fun save(item : User): User? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun findUserByUserId(userId : String) : User? {
        val item= User(userId)
        return try {
            awsConnector.getDynamoDBMapper().load(item)
        } catch (e : Exception) {
            null
        }
    }

    fun findUserByNickname(nickname : String) : User? {
        val dynamoUser = User().also { it.nickname = nickname }
        val queryExpression = DynamoDBQueryExpression<User>()
        queryExpression.indexName = "User-nickname"
        queryExpression.hashKeyValues = dynamoUser
        queryExpression.isConsistentRead = false
        val result = awsConnector.getDynamoDBMapper().query(User::class.java, queryExpression)
        return if(result.size == 0) {
            null
        }
        else {
            result.first()
        }
    }

    fun findUserByEmail(email : String) : User? {
        val dynamoUser = User().also { it.email = email }
        val queryExpression = DynamoDBQueryExpression<User>()
        queryExpression.indexName = "User-email"
        queryExpression.hashKeyValues = dynamoUser
        queryExpression.isConsistentRead = false
        val result = awsConnector.getDynamoDBMapper().query(User::class.java, queryExpression)
        return if(result.size == 0) {
            null
        }
        else {
            result.first()
        }
    }

}