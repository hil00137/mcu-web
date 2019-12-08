package com.mcu.controller

import com.mcu.model.Board
import com.mcu.model.BoardType
import com.mcu.service.BoardService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/board")
class BoardController {

    val logger = LoggerFactory.getLogger(this::class.java)!!

    @Autowired
    lateinit var boardService: BoardService

    /**
     * 정해진 게시판의 page별로 게시글들을 가져옴. Caching
     */
    @Cacheable(value = ["boardCache"])
    @GetMapping("/{type}/{page}")
    fun getBoards(@PathVariable type : String, @PathVariable page : String) : Map<String, Any> {
        logger.info("Get Boards type: $type, page : $page")
        var boardType: BoardType? = null
        var pageNum: Int
        try {
            boardType = BoardType.valueOf(type.toUpperCase())
            pageNum = page.toInt()
        } catch (e : IllegalArgumentException) {
            return HashMap()
        } catch (e : NumberFormatException) {
            pageNum = 0
        }

        val result = HashMap<String, Any>()
        result["count"] = boardService.getCountOfBoard(boardType!!)
        result["list"] = boardService.getBoards(boardType, pageNum)
        result["page"] = pageNum
        return result
    }

    /**
     * 게시글 등록
     */
    @PostMapping("/{type}")
    fun saveBoard(@PathVariable type: String, @RequestBody board: Board?) : String {
        val boardType: BoardType?
        try {
            boardType = BoardType.valueOf(type.toUpperCase())
        } catch (e : IllegalArgumentException) {
            return "존재하지 않는 게시판입니다."
        }
        board?:return "잘못된 접근입니다."
        val userId = SecurityContextHolder.getContext().authentication.principal as String
        board.userId = userId
        boardService.saveBoard(boardType, board)
        return "success"
    }
}