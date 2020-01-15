package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.DynamoBoard
import com.mcu.model.DynamoDeletedBoard
import com.mcu.model.McuQueryResultPage
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class DynamoBoardRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector

    @CacheEvict(value = ["boardCache"], allEntries = true)
    fun save(item: DynamoBoard) : DynamoBoard {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun delete(item : DynamoBoard) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DynamoDeletedBoard) : DynamoDeletedBoard? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun countByType(type : String): Int {
        val queryExpression = DynamoDBQueryExpression<DynamoBoard>()
        queryExpression.withIndexName("Board-type-regist").withConsistentRead(false).withHashKeyValues(DynamoBoard().also { it.type = type })
        return awsConnector.getDynamoDBMapper().count(DynamoBoard::class.java,queryExpression)
    }

    fun findById(id : String) : DynamoBoard? {
        return try {
            awsConnector.getDynamoDBMapper().load(DynamoBoard().also { it.id = id })
        } catch (e : Exception) {
            null
        }
    }

    @Cacheable(value = ["boardCache"])
    fun findAllByType(type: String, last : MutableMap<String, AttributeValue>): QueryResultPage<DynamoBoard>? {
        val queryExpression = DynamoDBQueryExpression<DynamoBoard>()
        queryExpression.withIndexName("Board-type-regist").withConsistentRead(false).withHashKeyValues(DynamoBoard().also { it.type = type })
                .withLimit(10).withScanIndexForward(false)
        if (last.isNotEmpty()) {
            queryExpression.withExclusiveStartKey(last)
        }
        return  awsConnector.getDynamoDBMapper().queryPage(DynamoBoard::class.java, queryExpression).let { McuQueryResultPage(it) }
    }
}