package com.mcu.repository

import com.mcu.model.MongoUser
import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoUserRepository : MongoRepository<MongoUser, String> {
    fun findUserByUserId(userId : String) : MongoUser?
    fun findUserByNickname(nickname : String) : MongoUser?
    fun findUserByEmail(email : String) : MongoUser?
}