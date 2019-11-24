package com.mcu.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/user")
class UserController {

    @GetMapping("/login")
    fun goLoginPage() : String {
        return "login"
    }
}