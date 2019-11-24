package com.mcu.controller

import com.mcu.service.McuServerManagementService
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var a : AwsConnector

    @GetMapping("/hello")
    fun hello(): String {
        return "HelloWorld"
    }
}