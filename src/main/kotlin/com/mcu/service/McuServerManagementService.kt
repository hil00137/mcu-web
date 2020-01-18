package com.mcu.service

import com.mcu.model.Server
import com.mcu.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class McuServerManagementService {

    @Autowired
    private lateinit var serverRepository: ServerRepository

    fun findByName(name : String) : Server? {
        return serverRepository.findByName(name)
    }

    fun getAllMcuServerList(): MutableList<Server> {
        return serverRepository.findAll()
    }

    fun updateProperty(server : Server): Server {
        return serverRepository.save(server)
    }
}