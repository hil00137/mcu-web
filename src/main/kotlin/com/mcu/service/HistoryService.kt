package com.mcu.service

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList
import com.mcu.model.History
import com.mcu.model.HistoryPriority
import com.mcu.model.Mail
import com.mcu.repository.HistoryRepository
import com.mcu.util.DateUtil
import com.mcu.util.MailSendUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class HistoryService {

    @Value("\${smtp.report}")
    private lateinit var reportMail : String

    @Autowired
    private lateinit var historyRepository : HistoryRepository

    @Autowired
    private lateinit var mailSendUtil: MailSendUtil
    /**
     * @param priority -> History Priority
     */
    fun writeHistory(message: String, priority : String) {
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
        history.priority = HistoryPriority.SYSTEM.name
        historyRepository.save(history)
    }

    @Scheduled(cron = "0 0 * ? * ?")
    fun reportHistory() {
        val currentDate = LocalDateTime.now(ZoneId.of("UTC"))
        val mail = Mail()
        mail.toName = "운영자"
        mail.toMail = reportMail
        mail.subject = "${DateUtil.transMailFormYmdh(currentDate)} 리포트 메일"

        val countMap = HashMap<String, Int>()
        val detailMap = HashMap<String, PaginatedList<History>?>()
        HistoryPriority.values().forEach {
            countMap[it.name] = historyRepository.getCountByPriorityForReport(it.name, currentDate)
            if(countMap[it.name] != 0 && (it == HistoryPriority.RULE_OVER || it == HistoryPriority.ERROR)) {
                detailMap[it.name] = historyRepository.getListByPriorityForReport(it.name, currentDate)
            }
        }

        mail.setReportContent(currentDate, countMap, detailMap)
        val result = mailSendUtil.sendEmail(mail)
        this.writeHistoryAsSystem("Report Send : ${result["result"]} , ${result["message"]}")
   }
}