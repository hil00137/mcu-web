package com.mcu.controller

import com.mcu.service.BoardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebCommonController {

    @Autowired
    lateinit var boardService : BoardService

    @GetMapping("/home")
    fun home(): String {
        return "home"
    }

    @GetMapping("/guide")
    fun guide(): String {
        return "guide"
    }
}