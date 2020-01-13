package com.mcu.model

import com.mcu.util.HashUtil
import java.time.LocalDateTime
import java.util.*

class Mail {
    companion object {
        const val NEW_LINE = "\r\n"
        const val brTag = "<br>"
    }
    lateinit var smtp : String
    lateinit var fromMail : String
    lateinit var toMail : String
    lateinit var toName : String
    lateinit var subject : String
    lateinit var content : String
    lateinit var header : String

    fun makeHeader() {
        val stringBuilder = StringBuilder()
        val base64 = Base64.getMimeEncoder()
        stringBuilder.append("Content-Type: text/html; charset=UTF-8$NEW_LINE")
        stringBuilder.append("From: =?UTF-8?B?${base64.encodeToString("마크대학 <$fromMail>".toByteArray())}?=$NEW_LINE")
        stringBuilder.append("To: =?UTF-8?B?${base64.encodeToString("$toName <$toMail>".toByteArray())}?=$NEW_LINE")
        stringBuilder.append("Date: ${LocalDateTime.now()}$NEW_LINE")
        stringBuilder.append("Subject: =?UTF-8?B?${base64.encodeToString(subject.toByteArray())}?=")
        this.header = stringBuilder.toString()
    }

    fun setEmailAuthContent(ip : String, url : String, user: User) {
        val userId = HashUtil.encryptAES256(user.userId)
        val mailAuthCode = HashUtil.encryptAES256(user.mailAuthCode!!)
        val stringBuilder = StringBuilder()
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 회원가입을 요청하였습니다. $brTag")
        stringBuilder.append("본인이 맞으시다면 아래 URL을 클릭해주시길 바랍니다.$brTag")
        stringBuilder.append("<a href=\"${url}/user/emailAuth?userId=${userId}&emailAuthCode=${mailAuthCode}\">메일 인증하기</a>$brTag")
        stringBuilder.append("감사합니다.$brTag")
        this.content = stringBuilder.toString()
    }
}