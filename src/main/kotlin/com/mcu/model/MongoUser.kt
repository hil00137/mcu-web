package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.LocalDateTime

@Deprecated("migration complete")
@Document(collection = "user")
class MongoUser : Serializable {
    @Id
    lateinit var id : String
    var userId : String = ""
    var password : String = ""
    var auth : String = "common"
    var regDate : LocalDateTime? = LocalDateTime.now()
    var email : String = ""
    var nickname : String = ""
    var mailAuth : String = "none"
    var mailAuthCode : String? = null
    var mailAuthFailReason : String? = null

    override fun toString() = "userId : $userId, email: $email, nickname: $nickname, auth: $auth"
}