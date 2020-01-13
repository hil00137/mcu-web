package com.mcu.service

import com.mcu.model.Mail
import com.mcu.model.User
import com.mcu.repository.UserRepository
import com.mcu.util.HashUtil
import com.mcu.util.MailSendUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

@Service
class UserService {

    @Autowired
    private lateinit var userRepository : UserRepository

    @Autowired
    private lateinit var mailSendUtil: MailSendUtil

    @Value("\${homepage.address}")
    private lateinit var homepageUrl : String

    @Cacheable(value = ["userCache"], key = "'userId:' + #userId")
    fun getUserByUserId(userId : String) = userRepository.findUserByUserId(userId)
    @Cacheable(value = ["userCache"], key = "'nickname:' + #nickname")
    fun getUserByNickname(nickname : String) = userRepository.findUserByNickname(nickname)
    @Cacheable(value = ["userCache"], key = "'email:' + #email")
    fun getUserByEmail(email : String) = userRepository.findUserByEmail(email)
    @CacheEvict(value = ["userCache"], allEntries = true)
    fun registerUser(user: User): User? {
        user.password = HashUtil.sha512(user.password)
        user.mailAuthCode = UUID.randomUUID().toString().replace("-","").substring(0,9)
        val ip = this.getIp()
        Thread() {
            val mail = Mail()
            mail.toMail = user.email
            mail.toName = user.nickname
            mail.subject = "마크대학 이메일 인증메일입니다"
            mail.setEmailAuthContent(ip, homepageUrl, user)
            val resultMap = mailSendUtil.sendEmail(mail)
            val targetUser = this.getUserByUserId(user.userId)!!
            if(resultMap["result"] == "success") {
                targetUser.mailAuth = "wait"
            } else if(resultMap["result"] == "fail") {
                targetUser.mailAuth = "fail"
                targetUser.mailAuthCode = null
                targetUser.mailAuthFailReason = resultMap["message"]
            }
            userRepository.save(targetUser)
        }.start()
        return userRepository.save(user)
    }

    fun save(user : User) : User? = userRepository.save(user)

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
}