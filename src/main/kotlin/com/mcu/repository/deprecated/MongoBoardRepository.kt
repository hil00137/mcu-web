package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoBoard
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

@Deprecated("migration complete")
interface MongoBoardRepository : PagingAndSortingRepository<MongoBoard, String> {
    fun countByTypeAndDelete(type : String, delete: Boolean) : Long
    fun findAllByTypeAndDelete(type: String, delete : Boolean, pageable: Pageable) : List<MongoBoard>
    @CacheEvict(value = ["boardCache"], allEntries = true)
    override fun <S : MongoBoard?> save(entity: S): S {
        return this.save(entity)
    }
}