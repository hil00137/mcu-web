package com.mcu.service

import com.mcu.model.Board
import com.mcu.model.BoardType
import com.mcu.model.History
import com.mcu.repository.BoardRepository
import com.mcu.util.DateUtil
import com.mcu.util.StringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BoardService {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var userService: UserService
    /**
     * 해당 게시판의 게시글 개수
     */
    fun getCountOfBoard(type : BoardType) = boardRepository.countByTypeAndDelete(type.type, false)

    /**
     * 해당 게시판의 해당 페이지 게시글
     */
    fun getBoards(type : BoardType, page : Int): List<Board> {
        val list = boardRepository.findAllByTypeAndDelete(type.type, false, PageRequest.of(page, 10, Sort.by("id").descending()))
        list.forEach {
            it.userId.let { userId -> it.nickname = userService.getUserByUserId(userId)?.nickname?:"" }
            it.regist?.let { regist -> it.formattedRegist = DateUtil.transform(regist)  }
            it.update?.let { update -> it.formattedUpdate = DateUtil.transform(update)  }
        }
        return list
    }

    /**
     * 게시글 저장 캐시 삭제
     */
    fun saveBoard(board: Board): Board {
        board.subject = StringUtil.checkScriptInjection(board.subject)
        if (StringUtil.isScriptInjection(board.subject)) {
            logger.warn("Script Injection occurred by ${board.userId}")
            board.subject = StringUtil.removeLabel(board.subject)
            historyService.writeHistory("Script Injection", History.RULE_OVER)
        }
        
        board.content = StringUtil.checkScriptInjection(board.content)
        if (StringUtil.isScriptInjection(board.content)) {
            logger.warn("Script Injection occurred by ${board.userId}")
            board.content = StringUtil.removeLabel(board.content)
        }
        return boardRepository.save(board)
    }

    /**
     * id로 검색하기
     */
    fun getBoardById(id: String) : Board? {
        val result = boardRepository.findById(id)
        return if(result.isPresent) {
            result.get()
        } else {
            null
        }
    }

    /**
     * 삭제처리 (삭제는 flag를 바꿔 기록을 남겨둔다.)
     */
    fun deleteBoard(board: Board) {
        board.delete = true
        board.deleteDate = LocalDateTime.now()
        boardRepository.save(board)
    }

    @Cacheable(value = ["hitCache"], key = "#userId + ':' + #board.id")
    fun hitCount(board: Board, userId : String) {
        board.hit++
        boardRepository.save(board)
    }

    /**
     * comment 숫자 늘리기
     */
    fun commentCount(board: Board, num: Int) {
        board.commentCount += num
        boardRepository.save(board)
    }
}