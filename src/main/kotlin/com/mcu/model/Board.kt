package com.mcu.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.LocalDateTime

@Document
class Board : Serializable {
    @Id
    lateinit var id : String                            // Mongodb 고유값
    lateinit var type : String                          // 게시판 타입
    var subject : String = ""                           // 게시글 제목
    var content : String = ""                           // 게시글 내용
    lateinit var userId : String                        // 작성자
    var nickname : String? = null
    var regist : LocalDateTime? = LocalDateTime.now()   // 작성일자
    var update : LocalDateTime? = LocalDateTime.now()   // 수정일자
    var formattedRegist : String? = null                // 정해진 양식의 작성일자 : 웹 출력용
    var formattedUpdate : String? = null                // 정해진 양식의 수정일자 : 웹 출력용
    var delete : Boolean = false                        // 삭제 여부
    var deleteDate : LocalDateTime? = null
}

/**
 * 게시판 구분
 */
enum class BoardType(val type: String) {
    SUGGESTION("suggestion")
}