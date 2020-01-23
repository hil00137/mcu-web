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

    fun sendEmail(prop : Properties) {
        println(prop)
        Thread {
            val mail = Mail(prop["user"] as User)
            when(prop["request"]) {
                "findFullId" -> {
                    mail.subject = "마크대학 아이디 찾기 메일입니다."
                    mail.setEmailFindFullId(prop["ip"] as String, (prop["user"] as User).userId!!)
                    mailSendUtil.sendEmail(mail)
                }
                "errorEmailNotify" -> {
                    mail.subject = "인증메일 전송에 실패하였습니다"
                    mail.setEmailChangeFailContent(prop["ip"] as String, prop["failEmail"] as String)
                    mailSendUtil.sendEmail(mail)
                }
                else -> {

                }
            }
        }.start()
    }
}