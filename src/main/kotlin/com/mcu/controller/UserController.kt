package com.mcu.controller

import com.mcu.model.HistoryPriority
import com.mcu.model.User
import com.mcu.service.HistoryService
import com.mcu.service.UserService
import com.mcu.util.HashUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
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

    @ResponseBody
    @PostMapping("/signUp")
    fun signUp(@RequestBody user: User?) : String {
        user?.let {
            userService.registerUser(it)?:"saved error"
        }?: return "error"
        historyService.writeHistory("Sign Up ${user.userId}",HistoryPriority.SYSTEM)
        return "OK"
    }

    @ResponseBody
    @GetMapping("/checkId/{userId}")
    fun idCheck(@PathVariable userId : String) : String {
        val user = userService.getUserByUserId(userId)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

    @ResponseBody
    @GetMapping("/checkNickname/{nickname}")
    fun nicknameCheck(@PathVariable nickname : String) : String {
        val decodeNickname = URLDecoder.decode(nickname,"UTF-8")
        val user = userService.getUserByNickname(decodeNickname)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

    @ResponseBody
    @GetMapping("/checkEmail/{email}")
    fun mailCheck(@PathVariable email : String) : String {
        val user = userService.getUserByEmail(email)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
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