package com.mcu.service

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.mcu.model.Comment
import com.mcu.model.DeletedComment
import com.mcu.repository.CommentRepository
import com.mcu.util.DateUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class CommentService {

    @Autowired
    lateinit var dynamoCommentRepository: CommentRepository

    @Autowired
    lateinit var userService : UserService

    /**
     * 해당 게시글의 댓글 가져오기
     */
    @Cacheable(value = ["commentCache"])
    fun getComments(boardId: String, page : Int): MutableList<Comment> {
        var resultPage : QueryResultPage<Comment>? = null
        for (i in 0 .. page) {
            val last = resultPage?.lastEvaluatedKey?:HashMap<String, AttributeValue>()
            resultPage = dynamoCommentRepository.findByBoardIdWithPage(boardId, last)
        }

        val list = resultPage?.results
        list?.forEach {
            it.userId.let { userId -> it.nickname = userService.getUserByUserId(userId)?.nickname?:"" }
            it.regist?.let { regist -> it.formattedRegist = DateUtil.transform(regist)  }
        }
        return list?: ArrayList()
    }

    fun saveComment(comment: Comment) =
            dynamoCommentRepository.save(comment)

    /**
     * 댓글 삭제 처리
     */
    fun deleteComment(comment: Comment) {
        val deleteComment = DeletedComment(comment)
        deleteComment.deleteDate = LocalDateTime.now()
        deleteComment.expire = LocalDateTime.now().plusDays(180).toEpochSecond(OffsetDateTime.now().offset)
        dynamoCommentRepository.saveBackupTable(deleteComment)
        dynamoCommentRepository.delete(comment)
    }

    fun deleteAll(boardId: String) {
        val list = dynamoCommentRepository.findAllByBoardId(boardId)
        list?.forEach {
            this.deleteComment(it)
        }
    }

    /**
     * id로 검색하기
     */
    fun getCommentById(id: String) : Comment? {
        return dynamoCommentRepository.findById(id)
    }
}