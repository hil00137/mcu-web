package com.mcu.controller

import com.mcu.model.Comment
import com.mcu.model.DeletedComment
import com.mcu.repository.CommentRepository
import com.mcu.repository.deprecated.MongoCommentRepository
import com.mcu.service.McuServerManagementService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@RestController
class WebRestController {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var commentRepository: MongoCommentRepository

    @Autowired
    lateinit var dynamoCommentRepository: CommentRepository

    @GetMapping("/migration/comment")
    fun moveUserData(): String {
        val list = commentRepository.findAll()

        for (comment in list) {
            logger.info(comment.content)
            if (comment.delete) {
                val deletedComment =  DeletedComment()
                deletedComment.id = UUID.randomUUID().toString()
                deletedComment.content = comment.content
                deletedComment.boardId = comment.boardId
                deletedComment.userId = comment.userId
                deletedComment.regist = comment.regist
                deletedComment.deleteDate = comment.deleteDate
                deletedComment.expire = LocalDateTime.now().plusDays(180).toEpochSecond(OffsetDateTime.now().offset)
                dynamoCommentRepository.saveBackupTable(deletedComment)
            } else {
                val dynamoComment  = Comment()
                dynamoComment.content = comment.content
                dynamoComment.boardId = comment.boardId
                dynamoComment.userId = comment.userId
                dynamoComment.regist = comment.regist
                dynamoCommentRepository.save(dynamoComment)
            }
        }
        return "success"
    }
}