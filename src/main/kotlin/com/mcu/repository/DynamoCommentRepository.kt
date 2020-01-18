package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.DeletedComment
import com.mcu.model.DynamoComment
import com.mcu.model.McuQueryResultPage
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class DynamoCommentRepository {
    @Autowired
    private lateinit var awsConnector: AwsConnector

    fun delete(item : DynamoComment) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DeletedComment) : DeletedComment? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun countByBoardId(boardId : String) : Int {
        val queryExpression = DynamoDBQueryExpression<DynamoComment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(DynamoComment().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().count(DynamoComment::class.java, queryExpression)
    }

    @Cacheable(value = ["commentCache"], key = "'id:'+#id")
    fun findById(id : String): DynamoComment? {
        return try {
            awsConnector.getDynamoDBMapper().load(DynamoComment().also { it.id = id })
        } catch (e: Exception) {
            null
        }
    }

    @Cacheable(value = ["commentCache"])
    fun findByBoardIdWithPage(boardId: String, last : MutableMap<String, AttributeValue>) : QueryResultPage<DynamoComment>? {
        val queryExpression = DynamoDBQueryExpression<DynamoComment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(DynamoComment().also { it.boardId = boardId })
                .withLimit(10).withScanIndexForward(false)
        if (last.isNotEmpty()) {
            queryExpression.withExclusiveStartKey(last)
        }
        return awsConnector.getDynamoDBMapper().queryPage(DynamoComment::class.java, queryExpression).let { McuQueryResultPage(it) }
    }

    fun findAllByBoardId(boardId: String): PaginatedQueryList<DynamoComment>? {
        val queryExpression = DynamoDBQueryExpression<DynamoComment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(DynamoComment().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().query(DynamoComment::class.java, queryExpression)
    }

    @CacheEvict(value = ["commentCache"], allEntries = true)
    fun save(item : DynamoComment) : DynamoComment {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}