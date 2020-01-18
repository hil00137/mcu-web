package com.mcu.controller

import com.mcu.repository.HistoryRepository
import com.mcu.service.McuServerManagementService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService


    @Autowired
    lateinit var dynamoHistoryRepository : HistoryRepository

    @GetMapping("/migration/history")
    fun moveUserData(): String {
        return "success"
    }
}