package com.mcu.controller

import com.mcu.model.BoardArchive
import com.mcu.repository.BoardArchiveRepository
import com.mcu.repository.deprecated.MongoBoardArchiveRepository
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var archiveRepository: MongoBoardArchiveRepository

    @Autowired
    lateinit var dynamoBoardArchiveRepository: BoardArchiveRepository

    @GetMapping("/migration/boardArchive")
    fun moveUserData(): String {
        val list = archiveRepository.findAll()
        for (boardArchive in list) {
            val dynamoBoardArchive  = BoardArchive()
            dynamoBoardArchive.boardId = boardArchive.boardId
            dynamoBoardArchive.oriSubject = boardArchive.oriSubject
            dynamoBoardArchive.oriContent = boardArchive.oriContent
            dynamoBoardArchive.modiSubject = boardArchive.modiSubject
            dynamoBoardArchive.modiContent = boardArchive.modiContent
            dynamoBoardArchive.modify = boardArchive.modify
            dynamoBoardArchiveRepository.save(dynamoBoardArchive)
        }
        return "success"
    }
}