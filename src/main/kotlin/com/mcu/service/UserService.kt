package com.mcu.service

import com.mcu.model.DynamoUser
import com.mcu.model.Mail
import com.mcu.model.MongoUser
import com.mcu.repository.DynamoUserRepository
import com.mcu.repository.MongoUserRepository
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
    private lateinit var userRepository : MongoUserRepository

    @Autowired
    private lateinit var mailSendUtil: MailSendUtil

    @Value("\${homepage.address}")
    private lateinit var homepageUrl : String

    @Autowired
    private lateinit var dynamoUserRepository : DynamoUserRepository

    @Cacheable(value = ["userCache"], key = "'userId:' + #userId")
    fun getUserByUserId(userId : String) = dynamoUserRepository.findUserByUserId(userId)
    @Cacheable(value = ["userCache"], key = "'nickname:' + #nickname")
    fun getUserByNickname(nickname : String) = dynamoUserRepository.findUserByNickname(nickname)
    @Cacheable(value = ["userCache"], key = "'email:' + #email")
    fun getUserByEmail(email : String) = dynamoUserRepository.findUserByEmail(email)
    @CacheEvict(value = ["userCache"], allEntries = true)
    fun registerUser(user: DynamoUser): DynamoUser? {
        user.password = HashUtil.sha512(user.password)
        user.mailAuthCode = UUID.randomUUID().toString().replace("-","").substring(0,9)
        val ip = this.getIp()
        Thread {
            val mail = Mail()
            mail.toMail = user.email!!
            mail.toName = user.nickname!!
            mail.subject = "마크대학 이메일 인증메일입니다"
            mail.setEmailAuthContent(ip, homepageUrl, user)
            val resultMap = mailSendUtil.sendEmail(mail)
            val targetUser = this.getUserByUserId(user.userId?:"")?:DynamoUser()

            if(resultMap["result"] == "success") {
                targetUser.mailAuth = "wait"
            } else if(resultMap["result"] == "fail") {
                targetUser.mailAuth = "fail"
                targetUser.mailAuthCode = null
                targetUser.mailAuthFailReason = resultMap["message"]
            }
            dynamoUserRepository.save(targetUser)
        }.start()
        return dynamoUserRepository.save(user)
    }

    fun save(user : MongoUser) : MongoUser? = userRepository.save(user)

    fun getIp() : String {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        var ip: String? = request.getHeader("X-FORWARDED-FOR")
        if (ip == null) {
            ip = request.remoteAddr
        }
        return ip?:""
    }

    fun emailAuthSuccess(user: DynamoUser) {
        user.mailAuth = "success"
        user.mailAuthCode = null
        dynamoUserRepository.save(user)
    }
}