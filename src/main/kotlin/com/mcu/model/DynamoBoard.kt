package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.mcu.configuration.DynamoDbConfig
import java.io.Serializable
import java.time.LocalDateTime

@DynamoDBTable(tableName = "Board")
class DynamoBoard : Serializable {
    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    var id : String? = null
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "Board-type-regist")
    lateinit var type : String                       // 게시판 타입
    @DynamoDBAttribute
    var subject : String = ""                           // 게시글 제목
    @DynamoDBAttribute
    var content : String = ""                           // 게시글 내용
    @DynamoDBAttribute
    lateinit var userId : String                      // 작성자
    var nickname : String? = null
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "Board-type-regist")
    var regist : LocalDateTime? = LocalDateTime.now()   // 작성일자
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    var update : LocalDateTime? = LocalDateTime.now()   // 수정일자
    var formattedRegist : String? = null                // 정해진 양식의 작성일자 : 웹 출력용
    var formattedUpdate : String? = null                // 정해진 양식의 수정일자 : 웹 출력용
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    var deleteDate : LocalDateTime? = null
    @DynamoDBAttribute
    var hit : Int = 0                                   // 조회수
    @DynamoDBAttribute
    var commentCount : Int = 0
}

@DynamoDBTable(tableName = "DeletedBoard")
class DynamoDeletedBoard() {
    constructor(board: DynamoBoard) : this() {
        this.id = board.id
        this.type = board.type
        this.subject = board.subject
        this.content = board.content
        this.userId = board.userId
        this.regist = board.regist
        this.hit = board.hit
        this.commentCount = board.commentCount
    }

    @DynamoDBHashKey
    var id : String? = null
    @DynamoDBAttribute
    lateinit var type : String                       // 게시판 타입
    @DynamoDBAttribute
    var subject : String = ""                           // 게시글 제목
    @DynamoDBAttribute
    var content : String = ""                           // 게시글 내용
    @DynamoDBAttribute
    lateinit var userId : String                      // 작성자
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    var regist : LocalDateTime? = LocalDateTime.now()   // 작성일자
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute
    var deleteDate : LocalDateTime? = null
    @DynamoDBAttribute
    var hit : Int = 0                                   // 조회수
    @DynamoDBAttribute
    var commentCount : Int = 0
}

/**
 * 게시판 구분
 */
enum class BoardType(val type: String) {
    SUGGESTION("suggestion"),
    NOTIFICATION("notification")
}