package com.mcu.service

import com.mcu.model.History
import com.mcu.repository.HistoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@Service
class HistoryService {

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    /**
     * @param priority -> History Priority
     */
    fun writeHistory(message: String, priority : Int) {
        val history = History()
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
        historyRepository.save(history)
    }

    fun writeHistoryAsSystem(message: String) {
        val history = History()
        history.userId = "admin"
        history.ip = "127.0.0.1"
        history.date = LocalDateTime.now()
        history.detail = message
        history.priority = History.SYSTEM
        historyRepository.save(history)
    }
}