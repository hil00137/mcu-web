package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class BoardArchive {
    @Id
    lateinit var id : String
    lateinit var boardId : String
    lateinit var oriSubject : String
    lateinit var oriContent : String
    lateinit var modiSubject : String
    lateinit var modiContent : String
    var modify : LocalDateTime = LocalDateTime.now()
}