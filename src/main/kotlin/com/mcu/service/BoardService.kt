package com.mcu.service

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.Board
import com.mcu.model.BoardType
import com.mcu.model.DeletedBoard
import com.mcu.model.History
import com.mcu.repository.BoardRepository
import com.mcu.util.DateUtil
import com.mcu.util.StringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

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
    fun getCountOfBoard(type : BoardType) = boardRepository.countByType(type.type)

    /**
     * 해당 게시판의 해당 페이지 게시글
     */
    @Cacheable(value = ["boardCache"])
    fun getBoards(type : BoardType, page : Int): MutableList<Board> {
        var resultPage : QueryResultPage<Board>? = null
        for (i in 0 .. page) {
            val last = resultPage?.lastEvaluatedKey?:HashMap<String, AttributeValue>()
            resultPage = boardRepository.findAllByType(type.type, last)
        }

        val list = resultPage?.results

        list?.forEach {
            it.userId.let { userId -> it.nickname = userService.getUserByUserId(userId)?.nickname?:"" }
            it.regist?.let { regist -> it.formattedRegist = DateUtil.transform(regist)  }
            it.update?.let { update -> it.formattedUpdate = DateUtil.transform(update)  }
        }
        return list?: ArrayList()
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
        return boardRepository.findById(id)
    }

    /**
     * 삭제처리 (삭제는 backup table로 옮긴다)
     */
    fun deleteBoard(board: Board) {
        val deletedBoard = DeletedBoard(board)
        deletedBoard.deleteDate = LocalDateTime.now()
        deletedBoard.expire = LocalDateTime.now().plusDays(180).toEpochSecond(OffsetDateTime.now().offset)
        boardRepository.delete(board)
        boardRepository.saveBackupTable(deletedBoard)
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