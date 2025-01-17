package com.mcu.service

import com.mcu.model.Board
import com.mcu.model.BoardArchive
import com.mcu.model.DeletedBoardArchive
import com.mcu.repository.BoardArchiveRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class BoardArchiveService {

    @Autowired
    private lateinit var boardArchiveRepository: BoardArchiveRepository

    fun archiving(oriBoard : Board, newBoard : Board) {
        val boardArchive = BoardArchive()
        boardArchive.boardId = oriBoard.id?:""
        boardArchive.oriSubject = oriBoard.subject
        boardArchive.oriContent = oriBoard.content
        boardArchive.modiSubject = newBoard.subject
        boardArchive.modiContent = newBoard.content
        boardArchiveRepository.save(boardArchive)
    }

    fun deleteAll(boardId: String) {
        val list = boardArchiveRepository.findAllByBoardId(boardId)
        list.forEach {
            val deletedBoardArchive = DeletedBoardArchive(it)
            deletedBoardArchive.delete = LocalDateTime.now()
            deletedBoardArchive.expire = LocalDateTime.now().plusDays(180).toEpochSecond(OffsetDateTime.now().offset)
            boardArchiveRepository.saveBackupTable(deletedBoardArchive)
            boardArchiveRepository.delete(it)
        }
    }
}