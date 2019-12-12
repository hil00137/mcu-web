package com.mcu.controller

import com.mcu.model.History
import com.mcu.model.User
import com.mcu.service.HistoryService
import com.mcu.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user")
class UserController {

    @Autowired
    lateinit var userService : UserService
    @Autowired
    lateinit var historyService: HistoryService

    @GetMapping("/login")
    fun goLoginPage() : String {
        return "login"
    }

    @GetMapping("/signUp")
    fun goSignPage() : String {
        return "signUp"
    }

    @ResponseBody
    @PostMapping("/signUp")
    fun signUp(@RequestBody user: User?) : String {
        user?.let {
            userService.registerUser(it)?:"saved error"
        }?: return "error"
        historyService.writeHistory("Sign Up ${user.userId}",History.SYSTEM)
        return "OK"
    }

    @ResponseBody
    @GetMapping("/checkId/{userId}")
    fun idCheck(@PathVariable userId : String) : String {
        val user = userService.getUserById(userId)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

    @Cacheable(value = ["userCache"], key = "#nickname")
    @ResponseBody
    @GetMapping("/checkNickname/{nickname}")
    fun nicknameCheck(@PathVariable nickname : String) : String {
        val user = userService.getUserByNickname(nickname)
        return if (user == null) {
            "OK"
        } else {
            "NO"
        }
    }

}