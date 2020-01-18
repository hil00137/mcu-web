package com.mcu.service

import com.mcu.model.DynamoHistory
import com.mcu.repository.DynamoHistoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@Service
class HistoryService {

    @Autowired
    private lateinit var dynamoHistoryRepository : DynamoHistoryRepository
    /**
     * @param priority -> History Priority
     */
    fun writeHistory(message: String, priority : String) {
        val history = DynamoHistory()
        val userId  = SecurityContextHolder.getContext().authentication.principal as String
        history.userId = userId
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        var ip: String? = request.getHeader("X-FORWARDED-FOR")
        if (ip == null) {
            ip = request.remoteAddr
        }
        history.ip = ip?:""
        history.date = LocalDateTime.now()
        history.detail = message
        history.priority = priority
        dynamoHistoryRepository.save(history)
    }

    fun writeHistoryAsSystem(message: String) {
        val history = DynamoHistory()
        history.userId = "admin"
        history.ip = "127.0.0.1"
        history.date = LocalDateTime.now()
        history.detail = message
        history.priority = DynamoHistory.SYSTEM
        dynamoHistoryRepository.save(history)
    }
}