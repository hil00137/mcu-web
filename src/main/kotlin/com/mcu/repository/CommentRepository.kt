package com.mcu.repository

import com.mcu.model.Comment
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface CommentRepository : PagingAndSortingRepository<Comment, String> {
    fun countByBoardIdAndDelete(boardId : String, delete: Boolean) : Long
    fun findAllByBoardIdAndDelete(boardId: String, delete : Boolean, pageable: Pageable) : List<Comment>
    @CacheEvict(value = ["commentCache"], allEntries = true)
    override fun <S : Comment?> save(entity: S): S {
        return this.save(entity)
    }
}