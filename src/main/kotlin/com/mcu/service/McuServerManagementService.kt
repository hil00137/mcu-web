package com.mcu.service

import com.mcu.model.DynamoServer
import com.mcu.repository.DynamoServerRepository
import com.mcu.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Service

@EnableMongoRepositories(basePackages = ["com.mcu.repository"])
@Service
class McuServerManagementService {

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var dynamoServerRepository: DynamoServerRepository

    fun findByName(name : String) : DynamoServer? {
        return dynamoServerRepository.findByName(name)
    }

    fun getAllMcuServerList(): MutableList<DynamoServer> {
        return dynamoServerRepository.findAll()
    }

    fun updateProperty(server : DynamoServer): DynamoServer {
        return dynamoServerRepository.save(server)
    }
}