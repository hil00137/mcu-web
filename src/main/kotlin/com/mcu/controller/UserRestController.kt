package com.mcu.controller

import com.mcu.model.HistoryPriority
import com.mcu.model.User
import com.mcu.service.HistoryService
import com.mcu.service.UserService
import com.mcu.util.HashUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.util.*
import kotlin.collections.HashMap

@RestController
@RequestMapping("/user")
class UserRestController {

    @Autowired
    private lateinit var userService : UserService

    @Autowired
    private lateinit var historyService : HistoryService

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

    @GetMapping("/info/detail/{id}")
    fun getUserDetail(@PathVariable id : String): HashMap<String, String> {
        val user = userService.getUserByUserId(id)
        val result = HashMap<String, String>()
        result["nickname"] = user?.nickname?:""
        result["email"] = user?.email?:""
        return result
    }

    @GetMapping("/checkEmail/{email}")
    fun mailCheck(@PathVariable email : String) : String {
        val user = userService.getUserByEmail(email)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

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

    @GetMapping("/checkId/{userId}")
    fun idCheck(@PathVariable userId : String) : String {
        val user = userService.getUserByUserId(userId)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

    @PostMapping("/signUp")
    fun signUp(@RequestBody user: User?) : String {
        user?.let {
            userService.registerUser(it)?:"saved error"
        }?: return "error"
        historyService.writeHistory("Sign Up ${user.userId}",HistoryPriority.SYSTEM)
        return "OK"
    }

    @PostMapping("/findInfo/id")
    fun findMyId(@RequestBody param : Properties) : HashMap<String, String> {
        val email = (param["email"] as String?)?:""
        val user = userService.getUserByEmail(email)
        val result = HashMap<String, String>()
        if(user == null) {
            result["code"] = "fail"
            result["message"] = "존재하지 않는 회원입니다. 이메일을 확인하거나, 회원가입을 해주시길 바랍니다."
            return result
        }

        result["code"] = "success"

        val stringBuilder = StringBuilder()
        (user.userId ?: "").forEachIndexed { index, c ->
            if (index < user.userId!!.length/2) {
                stringBuilder.append(c)
            } else {
                stringBuilder.append('*')
            }
        }

        result["userId"] = stringBuilder.toString()

        return result
    }

    @PostMapping("/findInfo/fullId")
    fun findMyFullId(@RequestBody param : Properties) : HashMap<String, String> {
        val email = (param["email"] as String?)?:""
        val user = userService.getUserByEmail(email)
        val result = HashMap<String, String>()
        if(user == null) {
            result["code"] = "fail"
            result["message"] = "잘못된 접근입니다."
            historyService.writeHistory("Access is not allowed.[find Id] from UNKNWON", HistoryPriority.RULE_OVER)
            return result
        }
        userService.findFullId(user)
        result["code"] = "success"
        result["message"] = "이메일을 확인해주시길 바랍니다.(스팸메시지함도 확인해주시길 바랍니다.)"
        historyService.writeHistory("Send email for finding user id ${user.userId}", HistoryPriority.USER_REQUEST)
        return result
    }

    @PostMapping("/findInfo/pwd")
    fun resetMyPassword(@RequestBody param : Properties) : HashMap<String, String> {
        val email = (param["email"] as String)?:""
        val id = (param["id"] as String)
        val user = userService.getUserByEmail(email)
        val result = HashMap<String, String>()
        if (user == null) {
            result["code"] = "fail"
            result["message"] = "잘못된 이메일입니다."
            historyService.writeHistory("Reset Password Fail By Wrong Email", HistoryPriority.USER_REQUEST)
            return result
        }

        if(user.userId != id) {
            result["code"] = "fail"
            result["message"] = "잘못된 아이디입니다. 아이디 찾기를 먼저 해주시길 바랍니다."
            historyService.writeHistory("Reset Password Fail By Wrong Id [${user.userId}]", HistoryPriority.USER_REQUEST)
            return result
        }
        userService.resetPassword(user)
        result["code"] = "success"
        result["message"] = "이메일을 확인해주시길 바랍니다."
        historyService.writeHistory("Send email for temporary password ${user.userId}", HistoryPriority.USER_REQUEST)
        return result
    }
}