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

        Thread {
            val mail = Mail(user)
            when (prop["request"]) {
                "findFullId" -> {
                    mail.subject = "마크대학 아이디 찾기 메일입니다."
                    mail.setEmailFindFullId(prop["ip"] as String, user.userId!!)
                    mailSendUtil.sendEmail(mail)
                }
                "errorEmailNotify" -> {
                    mail.subject = "인증메일 전송에 실패하였습니다"
                    mail.setEmailChangeFailContent(prop["ip"] as String, prop["failEmail"] as String)
                    mailSendUtil.sendEmail(mail)
                }
                "emailChangeMail" -> {
                    mail.subject = "마크대학 이메일 인증메일입니다"
                    mail.setEmailChangeContent(prop["ip"] as String, prop["url"] as String, user)
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