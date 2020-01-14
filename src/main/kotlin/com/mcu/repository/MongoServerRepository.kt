package com.mcu.repository

import com.mcu.model.MongoServer

import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoServerRepository : MongoRepository<MongoServer, String> {
    fun findByName(name : String) : MongoServer?
}