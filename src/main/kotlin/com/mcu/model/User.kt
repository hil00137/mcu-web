package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.mcu.configuration.DynamoDbConfig
import java.io.Serializable
import java.time.LocalDateTime

@DynamoDBTable(tableName = "User")
class User(@DynamoDBHashKey(attributeName = "userId") var userId: String? = "") : Serializable {

    @DynamoDBAttribute(attributeName = "password")
    var password : String = ""

    @DynamoDBAttribute(attributeName = "auth")
    var auth : String = "common"

    @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
    @DynamoDBAttribute(attributeName = "regDate")
    var regDate : LocalDateTime? = LocalDateTime.now()

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "User-email",attributeName = "email")
    var email : String? = null

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "User-nickname", attributeName = "nickname")
    var nickname : String? = null
    @DynamoDBAttribute(attributeName = "mailAuth")
    var mailAuth : String = "none"
    @DynamoDBAttribute(attributeName = "mailAuthCode")
    var mailAuthCode : String? = null
    @DynamoDBAttribute(attributeName = "mailAuthFailReason")
    var mailAuthFailReason : String? = null
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    @DynamoDBAttribute
    var isPasswordChange : Boolean? = null
}