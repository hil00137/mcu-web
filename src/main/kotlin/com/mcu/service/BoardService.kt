package com.mcu.service

import com.mcu.model.Board
import com.mcu.model.BoardType
import com.mcu.model.History
import com.mcu.repository.BoardRepository
import com.mcu.util.DateUtil
import com.mcu.util.StringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class BoardService {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var historyService: HistoryService

    /**
     * 해당 게시판의 게시글 개수
     */
    fun getCountOfBoard(type : BoardType) = boardRepository.countByType(type.type)

    /**
     * 해당 게시판의 해당 페이지 게시글
     */
    fun getBoards(type : BoardType, page : Int): List<Board> {
        val list = boardRepository.findAllByType(type.type, PageRequest.of(page, 10, Sort.by("id").descending()))
        list.forEach {
            it.regist?.let { regist -> it.formattedRegist = DateUtil.transform(regist)  }
            it.update?.let { update -> it.formattedUpdate = DateUtil.transform(update)  }
        }
        return list
    }

    /**
     * 게시글 저장 캐시 삭제
     */
    @CacheEvict(value = ["boardCache"], allEntries = true)
    fun saveBoard(type : BoardType, board: Board): Board {
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
            historyService.writeHistory("Script Injection", History.RULE_OVER)
        }
        return boardRepository.save(board)
    }
}