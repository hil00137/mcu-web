package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.mcu.model.Image
import com.mcu.model.McuQueryResultPage
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class ImageRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector

    fun save(item : Image): Image? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun delete(item : Image) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    /**
     * key기준으로 찾기
     */
    fun findByKey(key : String) : Image? {
        return try {
            awsConnector.getDynamoDBMapper().load(Image().also { it.key = key })
        } catch (e : Exception) {
            null
        }
    }

    /**
     * 게시글 번호 기준으로 찾기
     */
    fun findByBoardId(boardId : String) : McuQueryResultPage<Image> {
        val queryExpression = DynamoDBQueryExpression<Image>()
        queryExpression.withIndexName("Image-boardId-regist").withConsistentRead(false).withHashKeyValues(Image().also { it.boardId = boardId }).withScanIndexForward(false)
        return  awsConnector.getDynamoDBMapper().queryPage(Image::class.java, queryExpression).let { McuQueryResultPage(it) }
    }
}