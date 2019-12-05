package com.mcu.controller

import com.mcu.service.AwsManagementService
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/server")
class McuServerController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var awsManagementService : AwsManagementService

    @GetMapping("/status/{serverName}")
    fun serverStatus(@PathVariable serverName : String): Map<String, String> {
        val server = mcuServerManagementService.findByName(serverName) ?: return HashMap()
        val map = HashMap<String, String>()
        map["name"] = server.name
        map["online"] =  if (server.aws.online) {
            "on"
        } else {
            "off"
        }
        map["ip"] = server.ip
        map["started"] = server.aws.start?.toString()?:"Stopped"
        return map
    }

    @GetMapping("/start/{serverName}")
    fun startServer(@PathVariable serverName : String) : String {
        val server = mcuServerManagementService.findByName(serverName) ?: return "No matching server"
        if (server.aws.online) {
            return "Already Started. public_ip : ${server.ip}"
        }

        if (server.aws.code != 80) {
            return "서버를 시작할수 없는 상태입니다. 조금 있다가 다시 시작해주시길 바랍니다."
        }
        val result : Int
        try {
             result = awsManagementService.startInstance(server.aws.awsId.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            return "재시도 하여주시길 바랍니다. 지속적인 발생시 관리자에게 문의 바랍니다."
        }

        return if (result == 1) {
            "$serverName Server Starting"
        } else {
            "Why?"
        }
    }

    fun stopServer(@PathVariable serverName: String) : String {
        val server = mcuServerManagementService.findByName(serverName) ?: return "No matching server"
        if (!server.aws.online) {
            return "Already Stopped"
        }
        if (server.aws.code != 16) {
            return "서버를 끌 수 없는 상태입니다. 나중에 다시 시작해주시길 바랍니다."
        }

        val result : Int
        try {
            result = awsManagementService.stopInstance(server.aws.awsId.toString())
        } catch (e : Exception) {
            e.printStackTrace()
            return "재시도 하여주시길 바랍니다. 지속적인 발생시 관리자에게 문의 바랍니다."
        }
        return if (result == 1) {
            "$serverName Server Stopping"
        } else {
            "Why?"
        }
    }
}