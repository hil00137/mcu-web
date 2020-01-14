package com.mcu.controller

import com.mcu.model.DynamoUser
import com.mcu.repository.DynamoUserRepository
import com.mcu.repository.MongoUserRepository
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var dynamoUserRepository: DynamoUserRepository

    @Autowired
    lateinit var userRepository: MongoUserRepository

    @GetMapping("/migration/user")
    fun moveUserData(): String {
        val list = userRepository.findAll()
        for (user in list) {
            val dynamoUser = DynamoUser(userId = user.userId)
            dynamoUser.email = user.email
            dynamoUser.auth = user.auth
            dynamoUser.mailAuth = user.mailAuth
            dynamoUser.mailAuthCode = user.mailAuthCode
            dynamoUser.mailAuthFailReason = user.mailAuthFailReason
            dynamoUser.nickname = user.nickname
            dynamoUser.password = user.password
            dynamoUser.regDate = user.regDate
            dynamoUserRepository.save(dynamoUser)
        }
        return "success"
    }
}