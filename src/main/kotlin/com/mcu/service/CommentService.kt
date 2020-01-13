package com.mcu.service

import com.mcu.model.Comment
import com.mcu.repository.CommentRepository
import com.mcu.util.DateUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService {

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var userService : UserService

    /**
     * 해당 게시글의 댓글 갯수
     */
    fun getCountOfComment(boardId : String) = commentRepository.countByBoardIdAndDelete(boardId, false)

    /**
     * 해당 게시글의 댓글 가져오기
     */
    fun getComments(boardId: String, page : Int): List<Comment> {
        val list = commentRepository.findAllByBoardIdAndDelete(boardId, false, PageRequest.of(page, 5, Sort.by("id").descending()))
        list.forEach {
            it.userId.let { userId -> it.nickname = userService.getUserByUserId(userId)?.nickname?:"" }
            it.regist?.let { regist -> it.formattedRegist = DateUtil.transform(regist)  }
        }
        return list
    }

    fun saveComment(comment: Comment) =
            commentRepository.save(comment)

    /**
     * 댓글 삭제 처리
     */
    fun deleteComment(comment: Comment) {
        comment.delete = true
        comment.deleteDate = LocalDateTime.now()
        commentRepository.save(comment)
    }

    /**
     * id로 검색하기
     */
    fun getCommentById(id: String) : Comment? {
        val result = commentRepository.findById(id)
        return if(result.isPresent) {
            result.get()
        } else {
            null
        }
    }
}