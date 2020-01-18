package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoComment
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

@Deprecated("migration complete")
interface MongoCommentRepository : PagingAndSortingRepository<MongoComment, String> {
    fun countByBoardIdAndDelete(boardId : String, delete: Boolean) : Long
    fun findAllByBoardIdAndDelete(boardId: String, delete : Boolean, pageable: Pageable) : List<MongoComment>
    fun findAllByBoardId(boardId: String) : List<MongoComment>
    @CacheEvict(value = ["commentCache"], allEntries = true)
    override fun <S : MongoComment?> save(entity: S): S {
        return this.save(entity)
    }
}