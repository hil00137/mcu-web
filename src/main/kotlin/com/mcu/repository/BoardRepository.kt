package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.Board
import com.mcu.model.DeletedBoard
import com.mcu.model.McuQueryResultPage
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Repository

@Repository
class BoardRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector

    @Caching(evict = [
        CacheEvict(cacheNames = ["boardCache"], key = "'typeCount:'+#item.type"),
        CacheEvict(cacheNames = ["boardCache"], key = "'userIdCount:'+#item.userId"),
        CacheEvict(cacheNames = ["boardCache"], key = "'id:'+#item.id?:''"),
        CacheEvict(cacheNames = ["boardListCache"], allEntries = true)
    ])
    fun save(item: Board) : Board {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["boardCache"], key = "'typeCount:'+#item.type"),
        CacheEvict(cacheNames = ["boardCache"], key = "'userIdCount:'+#item.userId"),
        CacheEvict(cacheNames = ["boardCache"], key = "'id:'+#item.id"),
        CacheEvict(cacheNames = ["boardListCache"], allEntries = true)
    ])
    fun delete(item : Board) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DeletedBoard) : DeletedBoard? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    @Cacheable(value = ["boardCache"], key = "'typeCount:'+#type")
    fun countByType(type : String): Int {
        val queryExpression = DynamoDBQueryExpression<Board>()
        queryExpression.withIndexName("Board-type-regist").withConsistentRead(false).withHashKeyValues(Board().also { it.type = type })
        return awsConnector.getDynamoDBMapper().count(Board::class.java,queryExpression)
    }

    @Cacheable(value = ["boardCache"], key = "'userIdCount:'+#userId")
    fun countByUserId(userId : String) : Int {
        val queryExpression = DynamoDBQueryExpression<Board>()
        queryExpression.withIndexName("Board-userId-regist").withConsistentRead(false).withHashKeyValues(Board().also { it.userId = userId })
        return awsConnector.getDynamoDBMapper().count(Board::class.java,queryExpression)
    }

    @Cacheable(value = ["boardCache"], key = "'id:'+#id")
    fun findById(id : String) : Board? {
        return try {
            awsConnector.getDynamoDBMapper().load(Board().also { it.id = id })
        } catch (e : Exception) {
            null
        }
    }

    @Cacheable(value = ["boardListCache"], key = "'type:'+#type+':'+#last")
    fun findByTypeWithPage(type: String, last : MutableMap<String, AttributeValue>): QueryResultPage<Board>? {
        val queryExpression = DynamoDBQueryExpression<Board>()
        queryExpression.withIndexName("Board-type-regist").withConsistentRead(false).withHashKeyValues(Board().also { it.type = type })
                .withLimit(10).withScanIndexForward(false)
        if (last.isNotEmpty()) {
            queryExpression.withExclusiveStartKey(last)
        }
        return  awsConnector.getDynamoDBMapper().queryPage(Board::class.java, queryExpression).let { McuQueryResultPage(it) }
    }

    @Cacheable(value = ["boardListCache"], key = "'userId:'+ #userId+':'+#last")
    fun findByUserIdWithPage(userId: String, last : MutableMap<String, AttributeValue>): QueryResultPage<Board>? {
        val queryExpression = DynamoDBQueryExpression<Board>()
        queryExpression.withIndexName("Board-userId-regist").withConsistentRead(false).withHashKeyValues(Board().also { it.userId = userId })
                .withLimit(10).withScanIndexForward(false)
        if (last.isNotEmpty()) {
            queryExpression.withExclusiveStartKey(last)
        }
        return  awsConnector.getDynamoDBMapper().queryPage(Board::class.java, queryExpression).let { McuQueryResultPage(it) }
    }
}