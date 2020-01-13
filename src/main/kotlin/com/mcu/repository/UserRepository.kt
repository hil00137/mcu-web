package com.mcu.repository

import com.mcu.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findUserByUserId(userId : String) : User?
    fun findUserByNickname(nickname : String) : User?
    fun findUserByEmail(email : String) : User?
}