package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoBoardArchive
import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoBoardArchiveRepository : MongoRepository<MongoBoardArchive, String> {
    fun findAllByBoardId(boardId : String) : List<MongoBoardArchive>
}