package com.mcu.service

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.BoardType
import com.mcu.model.DynamoBoard
import com.mcu.model.DynamoDeletedBoard
import com.mcu.model.History
import com.mcu.repository.DynamoBoardRepository
import com.mcu.util.DateUtil
import com.mcu.util.StringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BoardService {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var dynamoBoardRepository: DynamoBoardRepository

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var userService: UserService
    /**
     * 해당 게시판의 게시글 개수
     */
    fun getCountOfBoard(type : BoardType) = dynamoBoardRepository.countByType(type.type)

    /**
     * 해당 게시판의 해당 페이지 게시글
     */
    @Cacheable(value = ["boardCache"])
    fun getBoards(type : BoardType, page : Int): MutableList<DynamoBoard> {
        var resultPage : QueryResultPage<DynamoBoard>? = null
        for (i in 0 .. page) {
            val last = resultPage?.lastEvaluatedKey?:HashMap<String, AttributeValue>()
            resultPage = dynamoBoardRepository.findAllByType(type.type, last)
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
    fun saveBoard(board: DynamoBoard): DynamoBoard {
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
        return dynamoBoardRepository.save(board)
    }

    /**
     * id로 검색하기
     */
    fun getBoardById(id: String) : DynamoBoard? {
        return dynamoBoardRepository.findById(id)
    }

    /**
     * 삭제처리 (삭제는 backup table로 옮긴다)
     */
    fun deleteBoard(board: DynamoBoard) {
        val deletedBoard = DynamoDeletedBoard(board)
        deletedBoard.deleteDate = LocalDateTime.now()
        dynamoBoardRepository.delete(board)
        dynamoBoardRepository.saveBackupTable(deletedBoard)
    }

    @Cacheable(value = ["hitCache"], key = "#userId + ':' + #board.id")
    fun hitCount(board: DynamoBoard, userId : String) {
        board.hit++
        dynamoBoardRepository.save(board)
    }

    /**
     * comment 숫자 늘리기
     */
    fun commentCount(board: DynamoBoard, num: Int) {
        board.commentCount += num
        dynamoBoardRepository.save(board)
    }
}