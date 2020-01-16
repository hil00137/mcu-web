package com.mcu.controller

import com.mcu.model.DynamoBoardArchive
import com.mcu.repository.BoardArchiveRepository
import com.mcu.repository.DynamoBoardArchiveRepository
import com.mcu.service.McuServerManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebRestController {

    @Autowired
    lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    lateinit var archiveRepository: BoardArchiveRepository

    @Autowired
    lateinit var dynamoBoardArchiveRepository: DynamoBoardArchiveRepository

    @GetMapping("/migration/boardArchive")
    fun moveUserData(): String {
        val list = archiveRepository.findAll()
        for (boardArchive in list) {
            val dynamoBoardArchive  = DynamoBoardArchive()
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