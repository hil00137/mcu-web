package com.mcu.service

import com.mcu.model.Board
import com.mcu.model.BoardArchive
import com.mcu.repository.BoardArchiveRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BoardArchiveService {

    @Autowired
    lateinit var boardArchiveRepository: BoardArchiveRepository

    fun archiving(oriBoard : Board, newBoard : Board) {
        val boardArchive = BoardArchive()
        boardArchive.boardId = oriBoard.id
        boardArchive.oriSubject = oriBoard.subject
        boardArchive.oriContent = oriBoard.content
        boardArchive.modiSubject = newBoard.subject
        boardArchive.modiContent = newBoard.content
        boardArchiveRepository.save(boardArchive)
    }
}