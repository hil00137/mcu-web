package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

@Document(collection = "server")
class Server {

    @Id lateinit var id : String
    @Field lateinit var name : String
    @Field var aws : Aws = Aws()
    @Field var minecraft : Minecraft = Minecraft()

    @Document(collection = "server.aws")
    class Aws {
        @Field("id") var id : String? = null
        var online : Boolean = false
        var start : Date? = null
        var update : Date? = null
    }

    @Document(collection = "server.minecraft")
    class Minecraft {
        var online : Boolean = false
        var now : Int = 0
        var max : Int = 0
        var start : Date? = null
        var update : Date? = null
    }
}