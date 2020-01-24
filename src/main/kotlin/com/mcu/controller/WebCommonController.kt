package com.mcu.controller

import com.mcu.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebCommonController {

    @Autowired
    lateinit var userService: UserService

    @GetMapping("/home")
    fun home(): String {
        val loginUserId = SecurityContextHolder.getContext().authentication.principal as String
        val user = userService.getUserByUserId(loginUserId)
        if(user?.isPasswordChange == true) {
            return "redirect:user/info"
        }
        return "home"
    }

    @GetMapping("/guide")
    fun guide(): String {
        return "guide"
    }
}