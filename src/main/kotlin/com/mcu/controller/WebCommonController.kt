package com.mcu.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebCommonController {

    @GetMapping("/home")
    fun home(): String {
        return "home"
    }
}