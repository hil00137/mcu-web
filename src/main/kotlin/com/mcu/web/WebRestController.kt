package com.mcu.web

import com.mcu.service.ServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var serverManagementService : ServerManagementService

    @GetMapping("/hello")
    fun hello(): String {
        return "HelloWorld"
    }

    @GetMapping("/server")
    fun server(): String {
        return serverManagementService.findByName("mcu_server_01").aws.online.toString()
    }

    @GetMapping("/update")
    fun update(): String {
        serverManagementService.updateProperty("mcu_server_01",false)
        return "ok"
    }
}