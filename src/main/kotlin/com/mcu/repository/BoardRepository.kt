package com.mcu.repository

import com.mcu.model.Board
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository


interface BoardRepository : PagingAndSortingRepository<Board, String> {
    fun countByTypeAndDelete(type : String, delete: Boolean) : Long
    fun findAllByTypeAndDelete(type: String, delete : Boolean, pageable: Pageable) : List<Board>
    @CacheEvict(value = ["boardCache"], allEntries = true)
    override fun <S : Board?> save(entity: S): S {
        return this.save(entity)
    }
}