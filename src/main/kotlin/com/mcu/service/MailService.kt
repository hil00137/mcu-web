package com.mcu.service

import com.mcu.model.Mail
import com.mcu.model.User
import com.mcu.util.MailSendUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class MailService {

    @Autowired
    private lateinit var mailSendUtil: MailSendUtil

    @Autowired
    private lateinit var userService: UserService

    fun sendEmail(prop : Properties) {
        val user = prop["user"] as User
        val ip = prop["ip"] as String

        Thread {
            val mail = Mail(user)
            when (prop["request"]) {
                "findFullId" -> {
                    mail.setEmailFindFullId(ip, user.userId!!)
                    mailSendUtil.sendEmail(mail)
                }
                "errorEmailNotify" -> {
                    mail.setEmailChangeFailContent(ip, prop["failEmail"] as String)
                    mailSendUtil.sendEmail(mail)
                }
                "emailChangeMail" -> {
                    mail.setEmailChangeContent(ip, prop["url"] as String, user)
                    val resultMap = mailSendUtil.sendEmail(mail)
                    resultMap["oriEmail"] = prop["oriEmail"] as String
                    resultMap["ip"] = prop["ip"] as String
                    resultMap["userId"] = user.userId?:""
                    userService.sendEmailChangeMailResult(resultMap, user)
                }
                else -> {

                }
            }

        }.start()
    }
}