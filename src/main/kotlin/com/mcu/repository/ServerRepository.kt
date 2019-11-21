package com.mcu.repository

import com.mcu.model.Server
import com.mongodb.MongoClient

import org.springframework.data.mongodb.repository.MongoRepository


interface ServerRepository : MongoRepository<Server, String> {
    fun findByName(name : String) : Server
}