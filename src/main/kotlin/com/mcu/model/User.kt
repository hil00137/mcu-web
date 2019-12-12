package com.mcu.model

import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.LocalDateTime

@Document(collection = "user")
class User : Serializable {
    var userId : String = ""
    var password : String = ""
    var auth : String = "common"
    var regDate : LocalDateTime? = LocalDateTime.now()
    var email : String = ""
    var nickname : String = ""

    override fun toString() = "userId : $userId, email: $email, nickname: $nickname, auth: $auth"
}