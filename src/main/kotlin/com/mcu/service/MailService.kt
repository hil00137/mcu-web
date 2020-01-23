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
            // 메일 세팅
            val mail = Mail(user)
            // 메일 내용 세팅
            when (prop["request"]) {
                "findFullId" -> {
                    mail.setEmailFindFullId(ip, user.userId!!)
                }
                "errorEmailNotify" -> {
                    mail.setEmailChangeFailContent(ip, prop["failEmail"] as String)
                }
                "emailChangeMail" -> {
                    mail.setEmailChangeContent(ip, prop["url"] as String, user)
                }
                "registerMail" -> {
                    mail.setEmailAuthContent(ip, prop["url"] as String, user)
                }
                "resetPwd" -> {
                    mail.setEmailResetPassword(ip, prop["pwd"] as String)
                }
                else -> {

                }
            }
            // 메일 발송 및 결과
            val resultMap = mailSendUtil.sendEmail(mail)

            // 결과 후 처리 작업필요할시
            when (prop["request"]) {
                "emailChangeMail" -> {
                    resultMap["oriEmail"] = prop["oriEmail"] as String
                    resultMap["ip"] = ip
                    resultMap["userId"] = user.userId?:""
                    userService.sendEmailChangeMailResult(resultMap)
                }
                "registerMail" -> {
                    resultMap["userId"] = user.userId?:""
                    userService.registerUserResult(resultMap)
                }
                else -> {

                }
            }

        }.start()
    }
}