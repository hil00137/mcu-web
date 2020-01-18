package com.mcu.controller

import com.mcu.model.DynamoHistory
import com.mcu.model.deprecated.MongoHistory
import com.mcu.repository.DynamoHistoryRepository
import com.mcu.repository.deprecated.MongoHistoryRepository
import com.mcu.service.McuServerManagementService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
class WebRestController {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var historyRepository: MongoHistoryRepository

    @Autowired
    lateinit var dynamoHistoryRepository : DynamoHistoryRepository

    @GetMapping("/migration/history")
    fun moveUserData(): String {
        val list = historyRepository.findAll()

        for (history in list) {
            logger.info(history.detail)
            val dynamoHistory = DynamoHistory()
            dynamoHistory.date = history.date
            dynamoHistory.detail = history.detail
            dynamoHistory.userId = history.userId
            dynamoHistory.priority = when(history.priority) {
                MongoHistory.RULE_OVER -> DynamoHistory.RULE_OVER
                MongoHistory.USER_REQUEST -> DynamoHistory.USER_REQUEST
                MongoHistory.SYSTEM -> DynamoHistory.SYSTEM
                MongoHistory.ERROR -> DynamoHistory.ERROR
                else -> ""
            }
            dynamoHistory.expire = history.date!!.plusYears(1).toEpochSecond(OffsetDateTime.now().offset)
            dynamoHistory.ip = history.ip
            dynamoHistoryRepository.save(dynamoHistory)
        }
        return "success"
    }
}