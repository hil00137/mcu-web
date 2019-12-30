package com.mcu.controller

import com.mcu.model.BoardType
import com.mcu.service.BoardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

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

    /**
     * detail page
     */
    @GetMapping("/board/detail/{id}")
    fun goDetail(@PathVariable id : String) : ModelAndView {
        return ModelAndView("board/detail").addObject("board", boardService.getBoardById(id))
    }

    @GetMapping("/board/write")
    fun goWrite(): String {
        return "board/write"
    }

    @GetMapping("/board")
    fun goBoardList(request : HttpServletRequest): ModelAndView {
        val boardType = request.getParameter("boardType")
        return ModelAndView("board/list").addObject("boardType",boardType?:BoardType.NOTIFICATION.type)
    }
}