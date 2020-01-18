package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.Comment
import com.mcu.model.DeletedComment
import com.mcu.model.McuQueryResultPage
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class CommentRepository {
    @Autowired
    private lateinit var awsConnector: AwsConnector

    fun delete(item : Comment) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DeletedComment) : DeletedComment? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun countByBoardId(boardId : String) : Int {
        val queryExpression = DynamoDBQueryExpression<Comment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(Comment().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().count(Comment::class.java, queryExpression)
    }

    @Cacheable(value = ["commentCache"], key = "'id:'+#id")
    fun findById(id : String): Comment? {
        return try {
            awsConnector.getDynamoDBMapper().load(Comment().also { it.id = id })
        } catch (e: Exception) {
            null
        }
    }

    @Cacheable(value = ["commentCache"])
    fun findByBoardIdWithPage(boardId: String, last : MutableMap<String, AttributeValue>) : McuQueryResultPage<Comment>? {
        val queryExpression = DynamoDBQueryExpression<Comment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(Comment().also { it.boardId = boardId })
                .withLimit(10).withScanIndexForward(false)
        if (last.isNotEmpty()) {
            queryExpression.withExclusiveStartKey(last)
        }
        return awsConnector.getDynamoDBMapper().queryPage(Comment::class.java, queryExpression).let { McuQueryResultPage(it) }
    }

    fun findAllByBoardId(boardId: String): PaginatedQueryList<Comment>? {
        val queryExpression = DynamoDBQueryExpression<Comment>()
        queryExpression.withIndexName("Comment-boardId-regist").withConsistentRead(false).withHashKeyValues(Comment().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().query(Comment::class.java, queryExpression)
    }

    @CacheEvict(value = ["commentCache"], allEntries = true)
    fun save(item : Comment) : Comment {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}