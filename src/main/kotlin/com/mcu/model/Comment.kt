package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.LocalDateTime

@Document
class Comment : Serializable {
    @Id
    lateinit var id : String
    lateinit var userId : String
    lateinit var boardId : String
    var content : String = ""
    var nickname : String? = null
    var regist : LocalDateTime? = LocalDateTime.now()
    var formattedRegist : String? = null
    var delete : Boolean = false                        // 삭제 여부
    var deleteDate : LocalDateTime? = null
}