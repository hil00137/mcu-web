package com.mcu.controller

import com.mcu.model.BoardType
import com.mcu.service.BoardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@RequestMapping("/board")
@Controller
class BoardController {

    @Autowired
    lateinit var boardService : BoardService


    /**
     * board detail page
     */
    @GetMapping("/detail/{id}")
    fun goBoardDetail(@PathVariable id : String) : ModelAndView {
        var targetBoard = boardService.getBoardById(id)
        if(targetBoard != null) {
            val userId = SecurityContextHolder.getContext().authentication.principal as String
            boardService.hitCount(targetBoard, userId)
        }
        return ModelAndView("board/detail").addObject("board", targetBoard)
    }

    @GetMapping("/write")
    fun goWrite(): String {
        return "board/write"
    }

    @GetMapping("")
    fun goBoardList(request : HttpServletRequest): ModelAndView {
        val boardType = request.getParameter("boardType")
        return ModelAndView("board/list").addObject("boardType",boardType?: BoardType.NOTIFICATION.type)
    }

    /**
     * 수정하기 위해 write page로
     */
    @PostMapping("/write")
    fun goModifyBoard(@ModelAttribute("boardId") boardId : String) : ModelAndView {
        if ("" == boardId) {
            return ModelAndView("board/write").addObject("message","잘못된 입력값입니다.")
        }
        val board = boardService.getBoardById(boardId) ?: return ModelAndView("board/write").addObject("message","잘못된 입력값입니다.")

        return ModelAndView("board/write").addObject("message","success").addObject("board", board)
    }
}