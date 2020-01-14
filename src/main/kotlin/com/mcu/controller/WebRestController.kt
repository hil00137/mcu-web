package com.mcu.controller

import com.mcu.model.Server
import com.mcu.repository.ServerRepository
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var dynamoUserRepository: ServerRepository

    @Autowired
    lateinit var dynamoServerRepository: ServerRepository

    @GetMapping("/migration/server")
    fun moveUserData(): String {
//        val list = serverRepository.findAll()
//        for (server in list) {
//            val dynamoServer = DynamoServer(name = server.name)
//            dynamoServer.ip = server.ip
//            dynamoServer.aws = Aws()
//            dynamoServer.aws.awsId = server.aws.awsId
//            dynamoServer.aws.online = server.aws.online
//            dynamoServer.aws.update = server.aws.update
//            dynamoServer.aws.start = server.aws.start
//            dynamoServer.aws.code = server.aws.code
//            dynamoServer.minecraft = Minecraft()
//            dynamoServer.minecraft.online = server.minecraft.online
//            dynamoServer.minecraft.zeroTime = server.minecraft.zeroTime
//            dynamoServer.minecraft.update = server.minecraft.update
//            dynamoServer.minecraft.now = server.minecraft.now
//            dynamoServer.minecraft.max = server.minecraft.max
//            dynamoServer.minecraft.link = server.minecraft.link
//            dynamoServerRepository.save(dynamoServer)
//        }
        return "success"
    }

    @GetMapping("/migration/test")
    fun getData(): String {
        val aaa = dynamoServerRepository.findByName("aaa")
        return if(aaa != null) {
            "OK"
        } else {
            "fail"
        }
    }

    @GetMapping("/migration/pushTest")
    fun pushData(): String {
        val bbb = Server("bbb")
//        bbb.aws?.awsId = "aba"

        val ccc = dynamoServerRepository.save(bbb)
        return if(ccc != null) {
            "OK"
        } else {
            "fail"
        }
    }
}