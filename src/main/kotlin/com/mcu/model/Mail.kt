package com.mcu.model

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList
import com.mcu.util.DateUtil
import com.mcu.util.HashUtil
import java.time.LocalDateTime
import java.util.*

class Mail() {
    constructor(user : User) : this() {
        this.toMail = user.email?:""
        this.toName = user.nickname?:""
    }
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
        stringBuilder.append("MIME-Version: 1.0$NEW_LINE")
        stringBuilder.append("Content-Type: text/html; charset=UTF-8$NEW_LINE")
        stringBuilder.append("From: =?UTF-8?B?${base64.encodeToString("마크대학".toByteArray())}?=<$fromMail>$NEW_LINE")
        stringBuilder.append("To: =?UTF-8?B?${base64.encodeToString(toName.toByteArray())}?=<$toMail>$NEW_LINE")
        stringBuilder.append("Date: ${LocalDateTime.now()}$NEW_LINE")
        stringBuilder.append("Subject: =?UTF-8?B?${base64.encodeToString(subject.toByteArray())}?=")
//        stringBuilder.append("Content-Transfer-Encoding: base64$NEW_LINE")
        this.header = stringBuilder.toString()
    }

    fun setEmailAuthContent(ip : String, url : String, user: User) {
        this.subject = "마크대학 이메일 인증메일입니다"
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>$NEW_LINE")
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag$NEW_LINE")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 회원가입을 요청하였습니다. $brTag$NEW_LINE")
        stringBuilder.append("본인이 맞으시다면 아래 URL을 클릭해주시길 바랍니다.$brTag$NEW_LINE")
        stringBuilder.append("<a href=\"${this.getAuthUrl(url, user)}\">메일 인증하기</a>$brTag$NEW_LINE")
        stringBuilder.append("감사합니다.$brTag$NEW_LINE")
        stringBuilder.append("</body></html>$NEW_LINE")
        this.content = stringBuilder.toString()
    }

    fun setEmailChangeContent(ip : String, url : String, user: User) {
        this.subject = "마크대학 이메일 인증메일입니다"
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>$NEW_LINE")
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag$NEW_LINE")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 메일변경을 요청하였습니다. $brTag$NEW_LINE")
        stringBuilder.append("본인이 맞으시다면 아래 URL을 클릭해주시길 바랍니다.$brTag$NEW_LINE")
        stringBuilder.append("<a href=\"${this.getAuthUrl(url, user)}\">메일 인증하기</a>$brTag$NEW_LINE")
        stringBuilder.append("감사합니다.$brTag$NEW_LINE")
        stringBuilder.append("</body></html>$NEW_LINE")
        this.content = stringBuilder.toString()
    }

    fun setEmailChangeFailContent(ip : String, email : String) {
        this.subject = "인증메일 전송에 실패하였습니다"
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>$NEW_LINE")
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag$NEW_LINE")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 이메일 변경을 요청하였습니다. $brTag$NEW_LINE")
        stringBuilder.append("하지만 해당 email : $email 에 메일을 전송하는동안 에러가 발생하였습니다.$brTag$NEW_LINE")
        stringBuilder.append("수신거부 혹은 존재하는 메일인지 확인하여 주시길바랍니다.$brTag$NEW_LINE")
        stringBuilder.append("등록하신 메일은 기존의 메일로 변경되었습니다.$brTag$NEW_LINE")
        this.content = stringBuilder.toString()
    }

    fun setReportContent(countMap: HashMap<String, Int>, detailMap : HashMap<String, PaginatedList<History>?>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>${NEW_LINE}")
        stringBuilder.append("${DateUtil.transMailFormYmdh(LocalDateTime.now())} 리포트$brTag$NEW_LINE")
        var check = false
        HistoryPriority.values().forEach {
            if(countMap[it.name] == 0) {
                return@forEach
            }
            check = true
            stringBuilder.append("${it.msgName} : ${countMap[it.name]}$brTag$NEW_LINE")
            detailMap[it.name]?.let { list ->
                stringBuilder.append("<table>")
                stringBuilder.append("<colgroup>")
                stringBuilder.append("<col span='1'>")
                stringBuilder.append("<col span='1'>")
                stringBuilder.append("<col span='1'>")
                stringBuilder.append("<col span='10'>")
                stringBuilder.append("</colgroup>$NEW_LINE")
                stringBuilder.append("<tr>")
                stringBuilder.append("<th>시간</th>")
                stringBuilder.append("<th>유저</th>")
                stringBuilder.append("<th>IP</th>")
                stringBuilder.append("<th>내용</th>")
                stringBuilder.append("</tr>$NEW_LINE")
                list.forEach { history ->
                    stringBuilder.append("<tr>")
                    stringBuilder.append("<th>${DateUtil.transMailFormHms(history.date!!)}</th>")
                    stringBuilder.append("<th>${history.userId}</th>")
                    stringBuilder.append("<th>${history.ip}</th>")
                    stringBuilder.append("<th>${history.detail}</th>")
                    stringBuilder.append("</tr>$NEW_LINE")
                }
                stringBuilder.append("</table>$NEW_LINE")
            }
        }

        if(!check) {
            stringBuilder.append("보고할 레포트가 없습니다.$brTag$NEW_LINE")
        }
        stringBuilder.append("</body></html>")
        this.content = stringBuilder.toString()
    }

    fun setEmailFindFullId(ip : String, userId : String) {
        this.subject = "마크대학 아이디 찾기 메일입니다."
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>$NEW_LINE")
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag$NEW_LINE")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 아이디를 요청하였습니다. $brTag$NEW_LINE")
        stringBuilder.append("${this.toName}님의 아이디는 [ <U>$userId</U> ] 입니다. $brTag$NEW_LINE")
        stringBuilder.append("감사합니다.$brTag$NEW_LINE")
        stringBuilder.append("</body></html>")
        this.content = stringBuilder.toString()
    }

    fun setEmailResetPassword(ip: String, pwd : String) {
        this.subject = "마크대학 비밀번호 초기화 메일입니다."
        val stringBuilder = StringBuilder()
        stringBuilder.append("<html><body>$NEW_LINE")
        stringBuilder.append("안녕하세요. 마크대학입니다. $brTag$NEW_LINE")
        stringBuilder.append("${ip}로부터 ${this.toName}님의 비밀번호 초기화를 요청하였습니다. $brTag$NEW_LINE")
        stringBuilder.append("${this.toName}님의 임시 비밀번호는 [ <U>$pwd</U> ] 입니다. $brTag$NEW_LINE")
        stringBuilder.append("감사합니다.$brTag$NEW_LINE")
        stringBuilder.append("</body></html>")
        this.content = stringBuilder.toString()
    }

    private fun getAuthUrl(url : String, user: User): String {
        val userId = HashUtil.encryptAES256(user.userId?:"")
        val mailAuthCode = HashUtil.encryptAES256(user.mailAuthCode!!)
        return "${url}/user/emailAuth?userId=${userId}&emailAuthCode=${mailAuthCode}"
    }
}