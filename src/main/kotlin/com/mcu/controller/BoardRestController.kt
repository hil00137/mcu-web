package com.mcu.controller

import com.mcu.model.Board
import com.mcu.model.BoardType
import com.mcu.model.History
import com.mcu.service.BoardArchiveService
import com.mcu.service.BoardService
import com.mcu.service.HistoryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/board")
class BoardRestController {

    val logger = LoggerFactory.getLogger(this::class.java)!!

    @Autowired
    lateinit var boardService: BoardService

    @Autowired
    lateinit var historyService : HistoryService

    @Autowired
    lateinit var boardArchiveService: BoardArchiveService

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
    fun saveBoard(@PathVariable type: String, @RequestBody board: Board?) : Map<String, Any> {
        val boardType: BoardType?
        val result = HashMap<String, Any>()
        try {
            boardType = BoardType.valueOf(type.toUpperCase())
        } catch (e : IllegalArgumentException) {
            result["message"] = "존재하지 않는 게시판입니다."
            return result
        }
        val userId = SecurityContextHolder.getContext().authentication.principal as String
        if(board == null) {
            result["message"]="잘못된 접근입니다."
            return result;
        } else if (boardType == BoardType.NOTIFICATION) {
            val list = SecurityContextHolder.getContext().authentication.authorities
            if(!list.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
                result["message"]="잘못된 접근입니다."
                historyService.writeHistory("Access is not allowed.[save board] from $userId", History.RULE_OVER)
                return result;
            }
        }
        board.userId = userId
        val boardId = boardService.saveBoard(board).id
        result["message"] = "success"
        result["boardId"] = boardId
        return result
    }

    /**
     * 게시글 삭제기능
     */
    @DeleteMapping("/{id}")
    fun deleteBoard(@PathVariable id : String?) : String {
        if (id == null || "" == id) {
            return "잘못된 입력값입니다."
        }
        val board = boardService.getBoardById(id) ?: return "잘못된 입력값입니다."
        val requestId = SecurityContextHolder.getContext().authentication.principal as String
        return if (board.userId == requestId ||
                SecurityContextHolder.getContext().authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            boardService.deleteBoard(board)
            historyService.writeHistory("delete board id : $id from $requestId", History.USER_REQUEST)
            "삭제하였습니다."
        } else {
            historyService.writeHistory("Access is not allowed.[delete board] from $requestId", History.RULE_OVER)
            "권한이 없습니다."
        }
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/write")
    fun modifyBoard(@RequestBody newBoard: Board?) : String {
        if(newBoard == null || newBoard.id == "") {
            return "잘못된 접근입니다."
        }
        val requestId = SecurityContextHolder.getContext().authentication.principal as String
        val oriBoard = boardService.getBoardById(newBoard.id)
        if(oriBoard == null) {
            historyService.writeHistory("Access is not allowed.[modify board] from $requestId", History.RULE_OVER)
            return "잘못된 접근입니다."
        } else if(oriBoard.userId != requestId) {
            historyService.writeHistory("Access is not allowed.[modify board] from $requestId", History.RULE_OVER)
            return "잘못된 접근입니다."
        }
        boardArchiveService.archiving(oriBoard, newBoard)
        oriBoard.subject = newBoard.subject
        oriBoard.content = newBoard.content
        oriBoard.update = LocalDateTime.now()
        boardService.saveBoard(oriBoard)
        return "success"
    }
}