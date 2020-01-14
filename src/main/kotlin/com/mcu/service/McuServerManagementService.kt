package com.mcu.service

import com.mcu.model.Server
import com.mcu.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Service

@EnableMongoRepositories(basePackages = ["com.mcu.repository"])
@Service
class McuServerManagementService {


    @Autowired
    private lateinit var dynamoServerRepository: ServerRepository

    fun findByName(name : String) : Server? {
        return dynamoServerRepository.findByName(name)
    }

    fun getAllMcuServerList(): MutableList<Server> {
        return dynamoServerRepository.findAll()
    }

    fun updateProperty(server : Server): Server {
        return dynamoServerRepository.save(server)
    }
}