package com.mcu.repository

import com.mcu.model.BoardArchive
import org.springframework.data.mongodb.repository.MongoRepository

interface BoardArchiveRepository : MongoRepository<BoardArchive, String> {
}