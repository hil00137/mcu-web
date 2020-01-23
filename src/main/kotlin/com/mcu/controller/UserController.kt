package com.mcu.controller

import com.mcu.model.HistoryPriority
import com.mcu.service.HistoryService
import com.mcu.service.UserService
import com.mcu.util.HashUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/user")
class UserController {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Autowired
    lateinit var userService : UserService
    @Autowired
    lateinit var historyService: HistoryService

    @GetMapping("/login")
    fun goLoginPage() : String {
        return "login"
    }

    @PostMapping("/login")
    fun goPostLoginPage() : String {
        return "login"
    }

    @GetMapping("/signUp")
    fun goSignPage() : String {
        return "signUp"
    }

    @GetMapping("/info")
    fun goUserInfoPage() : String {
        return "user/info"
    }

    @GetMapping("/emailAuth")
    fun emailAuth(request : HttpServletRequest) : String {
        val userId = request.getParameter("userId")?:""
        val emailAuthCode = request.getParameter("emailAuthCode")?:""
        if (userId.isEmpty() || emailAuthCode.isEmpty()) {
            request.setAttribute("errorMessage","잘못된 인증 URL입니다.")
            return "login"
        }

        val user = userService.getUserByUserId(HashUtil.decrpytAES256(userId))
        if (user == null) {
            request.setAttribute("errorMessage","잘못된 인증 URL입니다.")
            return "login"
        }

        if (user.mailAuth == "success") {
            request.setAttribute("errormessage", "만료된 인증 URL입니다.")
        }

        if (user.mailAuth == "wait" && user.mailAuthCode == HashUtil.decrpytAES256(emailAuthCode)) {
            request.setAttribute("errorMessage","이메일이 인증되었습니다.")
            logger.info("${user.userId} mail authenticate success")
            historyService.writeHistoryAsAdmin("${user.userId} mail authenticate success", HistoryPriority.USER_SIGN)
            userService.emailAuthSuccess(user)
        } else {
            request.setAttribute("errorMessage", "인증에 실패하였습니다.")
        }
        return "login"
    }
}