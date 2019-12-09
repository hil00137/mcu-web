package com.mcu.model

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class History {
    var priority : Int = 0              // 히스토리 타입 혹은 중요도
    var userId : String = "admin"       // 기본값으로 admin
    var ip : String = ""                // 해당 히스토리 발생 ip
    var detail : String = ""            // 히스토리 메시지
    var date : LocalDateTime? = null    // 해당 히스토리 생성 시간

    companion object {
        const val SYSTEM = 0
        const val USER_REQUEST = 1
        const val RULE_OVER = 2
        const val ERROR = 3
    }
}

