package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.mcu.model.DeletedBoardArchive
import com.mcu.model.DynamoBoardArchive
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoBoardArchiveRepository {

    @Autowired
    lateinit var awsConnector: AwsConnector

    fun findAllByBoardId(boardId : String) : List<DynamoBoardArchive> {
        val queryExpression = DynamoDBQueryExpression<DynamoBoardArchive>()
                .withIndexName("BoardArchive-boardId-modify").withConsistentRead(false).withHashKeyValues(DynamoBoardArchive().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().query(DynamoBoardArchive::class.java, queryExpression)
    }

    fun save(boardArchive: DynamoBoardArchive) {
        awsConnector.getDynamoDBMapper().save(boardArchive)
    }

    fun delete(item : DynamoBoardArchive) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DeletedBoardArchive) : DeletedBoardArchive? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}