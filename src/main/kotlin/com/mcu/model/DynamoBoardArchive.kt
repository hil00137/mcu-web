package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.mcu.configuration.DynamoDbConfig
import java.time.LocalDateTime

@DynamoDBTable(tableName = "BoardArchive")
class DynamoBoardArchive {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    var id : String? = null
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "BoardArchive-boardId-modify")
    lateinit var boardId : String
    @DynamoDBAttribute
    lateinit var oriSubject : String
    @DynamoDBAttribute
    lateinit var oriContent : String
    @DynamoDBAttribute
    lateinit var modiSubject : String
    @DynamoDBAttribute
    lateinit var modiContent : String
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "BoardArchive-boardId-modify")
    var modify : LocalDateTime = LocalDateTime.now()
}

@DynamoDBTable(tableName = "DeletedBoardArchive")
class DeletedBoardArchive() {
    constructor(boardArchive: DynamoBoardArchive) : this() {
        this.id = boardArchive.id
        this.boardId = boardArchive.boardId
        this.oriSubject = boardArchive.oriSubject
        this.oriContent = boardArchive.oriContent
        this.modiSubject = boardArchive.modiSubject
        this.modiContent = boardArchive.modiContent
        this.modify = boardArchive.modify
    }

    @DynamoDBHashKey
    var id : String? = null
    @DynamoDBAttribute
    lateinit var boardId : String
    @DynamoDBAttribute
    lateinit var oriSubject : String
    @DynamoDBAttribute
    lateinit var oriContent : String
    @DynamoDBAttribute
    lateinit var modiSubject : String
    @DynamoDBAttribute
    lateinit var modiContent : String
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    lateinit var modify : LocalDateTime
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    lateinit var delete : LocalDateTime
    @DynamoDBAttribute
    var expire : Long? = null
}