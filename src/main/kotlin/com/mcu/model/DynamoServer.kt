package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.mcu.configuration.DynamoDbConfig
import java.time.LocalDateTime

@DynamoDBTable(tableName = "Server")
class DynamoServer(@DynamoDBHashKey(attributeName = "name") var name: String = "") {
    @DynamoDBAttribute(attributeName = "ip")
    var ip: String = ""

    @DynamoDBAttribute(attributeName = "aws")
    lateinit var aws: Aws

    @DynamoDBAttribute(attributeName = "minecraft")
    lateinit var minecraft : Minecraft
    @DynamoDBDocument
    class Aws {
        @DynamoDBAttribute(attributeName = "awsId")
        var awsId: String? = null
        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
        @DynamoDBAttribute(attributeName = "online")
        var online: Boolean = false

        @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
        @DynamoDBAttribute
        var start: LocalDateTime? = null
        @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
        @DynamoDBAttribute
        var update: LocalDateTime? = null
        @DynamoDBAttribute
        var code: Int = 0
    }

    @DynamoDBDocument
    class Minecraft {
        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
        @DynamoDBAttribute
        var online : Boolean = false
        @DynamoDBAttribute
        var now : Int = 0
        @DynamoDBAttribute
        var max : Int = 0
        @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
        @DynamoDBAttribute
        var zeroTime : LocalDateTime? = null
        @DynamoDBTypeConverted(converter = DynamoDbConfig.LocalDateTimeConverter::class)
        @DynamoDBAttribute
        var update : LocalDateTime? = null
        @DynamoDBAttribute
        var link : String = ""
    }
}




