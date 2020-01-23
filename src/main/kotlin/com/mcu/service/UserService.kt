package com.mcu.service

import com.mcu.model.HistoryPriority
import com.mcu.model.User
import com.mcu.repository.UserRepository
import com.mcu.util.HashUtil
import com.mcu.util.MailSendUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

@Service
class UserService {

    @Autowired
    private lateinit var mailService : MailService

    @Autowired
    private lateinit var mailSendUtil: MailSendUtil

    @Value("\${homepage.address}")
    private lateinit var homepageUrl : String

    @Autowired
    private lateinit var userRepository : UserRepository

    @Autowired
    private lateinit var historyService : HistoryService

    @Cacheable(value = ["userCache"], key = "'userId:' + #userId")
    fun getUserByUserId(userId : String) = userRepository.findUserByUserId(userId)
    @Cacheable(value = ["userCache"], key = "'nickname:' + #nickname")
    fun getUserByNickname(nickname : String) = userRepository.findUserByNickname(nickname)
    @Cacheable(value = ["userCache"], key = "'email:' + #email")
    fun getUserByEmail(email : String) = userRepository.findUserByEmail(email)

    fun update(user: User) = userRepository.save(user)!!

    fun sendEmailChangeMail(user : User, oriEmail : String): User? {
        user.mailAuthCode = UUID.randomUUID().toString().replace("-","").substring(0,9)
        val prop = Properties()
        prop["ip"] = this.getIp()
        prop["request"] = "emailChangeMail"
        prop["url"] = homepageUrl
        prop["oriEmail"] = oriEmail
        prop["user"] = user
        mailService.sendEmail(prop)
        return userRepository.save(user)
    }

    fun sendEmailChangeMailResult(result : HashMap<String, String>) {
        val targetUser = this.getUserByUserId(result["userId"]!!)?:User()
        val oriEmail = result["oriEmail"]
        val ip = result["ip"]
        if(result["result"] == "success") {
            targetUser.mailAuth = "wait"
        } else if(result["result"] == "fail") {
            val failEmail = targetUser.email
            historyService.writeHistoryAsAdmin("Email Change Fail $oriEmail -> $failEmail", HistoryPriority.ERROR)
            targetUser.mailAuth = "success"
            targetUser.email = oriEmail
            this.errorEmailNotify(targetUser, failEmail?:"", ip?:"")
        }
        userRepository.save(targetUser)
    }

    fun errorEmailNotify(user: User, failEmail : String, ip : String) {
        val prop = Properties()
        prop["request"] = "errorEmailNotify"
        prop["user"] = user
        prop["ip"] = ip
        prop["failEmail"] = failEmail
        mailService.sendEmail(prop)
    }

    fun registerUser(user: User): User? {
        user.password = HashUtil.sha512(user.password)
        user.mailAuthCode = UUID.randomUUID().toString().replace("-","").substring(0,9)
        val prop = Properties()
        prop["ip"] = this.getIp()
        prop["request"] = "registerMail"
        prop["url"] = homepageUrl
        prop["user"] = user
        mailService.sendEmail(prop)
        return userRepository.save(user)
    }

    fun registerUserResult(result : HashMap<String, String>) {
        val targetUser = this.getUserByUserId(result["userId"]!!)?:User()
        if(result["result"] == "success") {
            targetUser.mailAuth = "wait"
        } else if(result["result"] == "fail") {
            targetUser.mailAuth = "fail"
            targetUser.mailAuthCode = null
            targetUser.mailAuthFailReason = result["message"]
        }
        userRepository.save(targetUser)
    }

    fun getIp() : String {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        var ip: String? = request.getHeader("X-FORWARDED-FOR")
        if (ip == null) {
            ip = request.remoteAddr
        }
        return ip?:""
    }

    fun emailAuthSuccess(user: User) {
        user.mailAuth = "success"
        user.mailAuthCode = null
        userRepository.save(user)
    }

    fun findFullId(user : User) {
        val prop = Properties()
        prop["request"] = "findFullId"
        prop["user"] = user
        prop["ip"] = this.getIp()
        mailService.sendEmail(prop)
    }

    fun resetPassword(user : User) {
        val prop = Properties()
        val pwd =  UUID.randomUUID().toString().replace("-","").substring(0,9)
        user.password = HashUtil.sha512(pwd)
        prop["request"] = "resetPwd"
        prop["user"] = user
        prop["ip"] = this.getIp()
        prop["pwd"] = pwd
        userRepository.save(user)
        mailService.sendEmail(prop)
    }
}