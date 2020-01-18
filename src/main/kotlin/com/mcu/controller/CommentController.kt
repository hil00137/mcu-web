package com.mcu.controller

import com.mcu.model.Board
import com.mcu.model.Comment
import com.mcu.model.History
import com.mcu.service.BoardService
import com.mcu.service.CommentService
import com.mcu.service.HistoryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/comment")
class CommentController {

    val logger = LoggerFactory.getLogger(this::class.java)!!

    @Autowired
    lateinit var commentService: CommentService

    @Autowired
    lateinit var boardService: BoardService

    @Autowired
    lateinit var historyService : HistoryService

    /**
     * 댓글 저장
     */
    @PostMapping("")
    fun saveComment(@RequestBody comment : Comment) : String {
        val userId = SecurityContextHolder.getContext().authentication.principal as String
        val boardId: String? = comment.boardId
        val board: Board? = boardId?.let {
            boardService.getBoardById(it)
        }
        if (board == null) {
            historyService.writeHistory("Access is not allowed.[save board] from $userId", History.RULE_OVER)
            return "잘못된 접근입니다."
        }
        comment.userId = userId
        boardService.commentCount(board, 1)
        commentService.saveComment(comment)
        return "success"
    }

    /**
     * 댓글들 가져오기
     */
    @GetMapping("/{boardId}/{page}")
    fun getComments(@PathVariable boardId: String, @PathVariable page: String) : Map<String, Any> {
        logger.info("Get comments id: $boardId, page : $page")
        var pageNum: Int
        var board : Board? = null
        try {
            board = boardService.getBoardById(boardId) ?: return HashMap()
            pageNum = page.toInt()
        } catch (e : NumberFormatException) {
            pageNum = 0
        }

        val result = HashMap<String, Any>()
        result["count"] = board?.commentCount?:0
        result["list"] = commentService.getComments(boardId, pageNum)
        result["page"] = pageNum
        return result
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("")
    fun deleteComment(@RequestBody param : HashMap<String, String>) : String {
        val commentId = param["commentId"]

        if (commentId == null || "" == commentId) {
            return "잘못된 입력값입니다."
        }
        val comment = commentService.getCommentById(commentId) ?: return "잘못된 입력값입니다."
        val requestId = SecurityContextHolder.getContext().authentication.principal as String
        return if (comment.userId == requestId ||
                SecurityContextHolder.getContext().authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            commentService.deleteComment(comment)
            boardService.commentCount(boardService.getBoardById(comment.boardId)!!, -1)
            "success"
        } else {
            historyService.writeHistory("Access is not allowed.[delete comment] from $requestId", History.RULE_OVER)
            "권한이 없습니다."
        }
    }
}