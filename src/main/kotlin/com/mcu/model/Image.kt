package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.mcu.configuration.DynamoDbConfig
import java.time.LocalDateTime

@DynamoDBTable(tableName = "Image")
class Image() {
    constructor(key : String) : this() {
        this.key = key
    }

    @DynamoDBHashKey
    var key : String? = null
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "Image-boardId-regist")
    var boardId : String? = null
    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "Image-boardId-regist")
    var regist : LocalDateTime? = null
    @DynamoDBAttribute
    var userId : String? = null
    @DynamoDBAttribute
    var url : String? = null
    @DynamoDBAttribute
    var oriFileName : String? = null
    @DynamoDBAttribute
    var contentType : String? = null
    @DynamoDBAttribute
    var refBoardList : ArrayList<String>? = null
}