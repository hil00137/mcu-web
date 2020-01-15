package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoServer

import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoServerRepository : MongoRepository<MongoServer, String> {
    fun findByName(name : String) : MongoServer?
}