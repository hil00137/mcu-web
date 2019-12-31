package com.mcu.controller

import com.mcu.model.BoardType
import com.mcu.service.BoardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
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
     * board detail page
     */
    @GetMapping("/board/detail/{id}")
    fun goBoardDetail(@PathVariable id : String) : ModelAndView {
        var targetBoard = boardService.getBoardById(id)
        if(targetBoard != null) {
            val userId = SecurityContextHolder.getContext().authentication.principal as String
            boardService.hitCount(targetBoard, userId)
        }
        return ModelAndView("board/detail").addObject("board", targetBoard)
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