package com.mcu.service

import com.mcu.model.Server
import com.mcu.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Service

@EnableMongoRepositories(basePackages = ["com.mcu.repository"])
@Service
class ServerManagementService {

    @Autowired
    private lateinit var serverRepository: ServerRepository

    fun findByName(name : String) : Server {
        return serverRepository.findByName(name)
    }

    fun getAllServerProperty(): MutableList<Server> {
        return serverRepository.findAll()
    }

    fun updateProperty(name: String ,online: Boolean): Server {
        val server = findByName(name)
        server.aws.online = online
        return serverRepository.save(server)
    }
}