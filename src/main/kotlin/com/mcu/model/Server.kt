package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "server")
class Server {

    @Id lateinit var id : String
    @Field lateinit var name : String
    @Field lateinit var ip : String
    @Field lateinit var aws : Aws
    @Field lateinit var minecraft : Minecraft

    @Document(collection = "server.aws")
    class Aws {
        @Field var awsId : String? = null
        var online : Boolean = false
        var start : LocalDateTime? = null
        var update : LocalDateTime? = null
        var code : Int = 0
    }

    @Document(collection = "server.minecraft")
    class Minecraft {
        var online : Boolean = false
        var now : Int = 0
        var max : Int = 0
        var zeroTime : LocalDateTime? = null
        var update : LocalDateTime? = null
        @Field var link : String = ""
    }
}