package com.mcu.controller

import com.mcu.model.HistoryPriority
import com.mcu.model.User
import com.mcu.service.HistoryService
import com.mcu.service.UserService
import com.mcu.util.HashUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
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

    @ResponseBody
    @GetMapping("/info/detail/{id}")
    fun getUserDetail(@PathVariable id : String): HashMap<String, String> {
        val user = userService.getUserByUserId(id)
        val result = HashMap<String, String>()
        result["nickname"] = user?.nickname?:""
        result["email"] = user?.email?:""
        return result
    }

    @ResponseBody
    @PostMapping("/info/checkPassword")
    fun isPasswordOk(@RequestBody user : User): HashMap<String, String> {
        val requestUserId = user.userId?:""
        val loginUserId = SecurityContextHolder.getContext().authentication.principal as String
        val result = HashMap<String, String>()
        if (requestUserId != loginUserId) {
            historyService.writeHistory("Access is not allowed.[password check] from $loginUserId", HistoryPriority.RULE_OVER)
            result["code"] = "fail"
            result["message"] = "잘못된 접근입니다."
            return result
        }
        val searchUser = userService.getUserByUserId(requestUserId)!!
        if(HashUtil.sha512(user.password) != searchUser.password) {
            result["code"] = "fail"
            result["message"] = "비밀번호가 틀렸습니다."
            return result
        }
        result["code"] = "success"
        return result
    }

    @ResponseBody
    @PutMapping("/info/change")
    fun changeInfo(@RequestBody user : User) : HashMap<String, String> {
        val requestUserId = user.userId?:""
        val loginUserId = SecurityContextHolder.getContext().authentication.principal as String
        val result = HashMap<String, String>()
        if (requestUserId != loginUserId) {
            historyService.writeHistory("Access is not allowed.[info Change] from $loginUserId", HistoryPriority.RULE_OVER)
            result["code"] = "fail"
            result["message"] = "잘못된 접근입니다."
            return result
        }
        val searchUser = userService.getUserByUserId(requestUserId)!!

        if (user.nickname != searchUser.nickname) {
            historyService.writeHistory("Nickname Change ${searchUser.nickname} -> ${user.nickname}", HistoryPriority.USER_REQUEST)
            searchUser.nickname = user.nickname
            userService.update(searchUser)
            result["code"] = "success"
            result["url"] = "/user/info"
            result["message"] = "닉네임 변경이 완료되었습니다."
        }

        if (user.password != "") {
            if(searchUser.password == HashUtil.sha512(user.password)) {
                result["code"] = "fail"
                result["message"] = "비밀번호가 같습니다."
                return result
            }
            searchUser.password = HashUtil.sha512(user.password)
            userService.update(searchUser)
            result["code"] = "success"
            result["url"] = "/user/logout"
            result["message"] = "비밀번호 변경이 완료되었습니다. 다시 로그인해주시길 바랍니다."
        }

        if (user.email != searchUser.email) {
            historyService.writeHistory("Email Change ${searchUser.email} -> ${user.email}", HistoryPriority.USER_REQUEST)
            val oriEmail = searchUser.email!!
            searchUser.email = user.email
            userService.sendEmailChangeMail(searchUser, oriEmail)
            result["code"] = "success"
            result["url"] = "/user/logout"
            result["message"] = "이메일을 변경하였습니다. 인증메일 확인후 다시 로그인해주시길 바랍니다."
        }
        return result
    }
}