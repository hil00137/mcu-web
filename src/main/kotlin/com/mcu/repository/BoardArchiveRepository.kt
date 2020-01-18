package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.mcu.model.BoardArchive
import com.mcu.model.DeletedBoardArchive
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class BoardArchiveRepository {

    @Autowired
    lateinit var awsConnector: AwsConnector

    fun findAllByBoardId(boardId : String) : List<BoardArchive> {
        val queryExpression = DynamoDBQueryExpression<BoardArchive>()
                .withIndexName("BoardArchive-boardId-modify").withConsistentRead(false).withHashKeyValues(BoardArchive().also { it.boardId = boardId })
        return awsConnector.getDynamoDBMapper().query(BoardArchive::class.java, queryExpression)
    }

    fun save(boardArchive: BoardArchive) {
        awsConnector.getDynamoDBMapper().save(boardArchive)
    }

    fun delete(item : BoardArchive) {
        awsConnector.getDynamoDBMapper().delete(item)
    }

    fun saveBackupTable(item: DeletedBoardArchive) : DeletedBoardArchive? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }
}