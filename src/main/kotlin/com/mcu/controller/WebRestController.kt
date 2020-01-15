package com.mcu.controller

import com.mcu.model.BoardType
import com.mcu.model.DynamoBoard
import com.mcu.model.DynamoDeletedBoard
import com.mcu.repository.BoardArchiveRepository
import com.mcu.repository.CommentRepository
import com.mcu.repository.DynamoBoardRepository
import com.mcu.repository.deprecated.MongoBoardRepository
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.collections.HashMap

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var boardRepository: MongoBoardRepository

    @Autowired
    lateinit var dynamoBoardRepository: DynamoBoardRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var archiveRepository: BoardArchiveRepository

    @GetMapping("/migration/board")
    fun moveUserData(): String {
        val list = boardRepository.findAll()
        for (board in list) {
            if(!board.delete) {
                val dynamoBoard = DynamoBoard()
                dynamoBoard.content = board.content
                dynamoBoard.commentCount = board.commentCount
                dynamoBoard.content = board.content
                dynamoBoard.content = board.content
                dynamoBoard.deleteDate = board.deleteDate
                dynamoBoard.hit = board.hit
                dynamoBoard.regist = board.regist
                dynamoBoard.subject = board.subject
                dynamoBoard.update = board.update
                dynamoBoard.userId = board.userId
                dynamoBoard.type = board.type
                val result = dynamoBoardRepository.save(dynamoBoard)
                val commentList = commentRepository.findAllByBoardId(board.id)
                commentList.forEach{
                    it.boardId = result.id!!
                    commentRepository.save(it)
                }
                val archiveList = archiveRepository.findAllByBoardId(board.id)
                archiveList.forEach{
                    it.boardId = result.id!!
                    archiveRepository.save(it)
                }

            } else {
                val dynamoBoard = DynamoDeletedBoard()
                dynamoBoard.id = UUID.randomUUID().toString()
                dynamoBoard.content = board.content
                dynamoBoard.commentCount = board.commentCount
                dynamoBoard.content = board.content
                dynamoBoard.deleteDate = board.deleteDate
                dynamoBoard.hit = board.hit
                dynamoBoard.regist = board.regist
                dynamoBoard.subject = board.subject
                dynamoBoard.userId = board.userId
                dynamoBoard.type = board.type
                dynamoBoard.deleteDate = board.deleteDate
                val result = dynamoBoardRepository.saveBackupTable(dynamoBoard)
                val commentList = commentRepository.findAllByBoardId(board.id)
                commentList.forEach{
                    it.boardId = result!!.id!!
                    commentRepository.save(it)
                }
                val archiveList = archiveRepository.findAllByBoardId(board.id)
                archiveList.forEach{
                    it.boardId = result!!.id!!
                    archiveRepository.save(it)
                }
            }
        }
        return "success"
    }

    @GetMapping("/migration/test")
    fun getData(): String {

        val page = dynamoBoardRepository.findAllByType(BoardType.SUGGESTION.type, HashMap())!!
        println(page.lastEvaluatedKey)
        return page.results.size.toString()
    }

    @GetMapping("/migration/pushTest")
    fun pushData(): String {
        val bbb = DynamoBoard()
        bbb.content = "adsfasdf"
        val ccc = dynamoBoardRepository.save(bbb)
        return if(ccc != null) {
            "OK"
        } else {
            "fail"
        }
    }
}