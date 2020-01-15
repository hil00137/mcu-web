package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoUser
import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoUserRepository : MongoRepository<MongoUser, String> {
    fun findUserByUserId(userId : String) : MongoUser?
    fun findUserByNickname(nickname : String) : MongoUser?
    fun findUserByEmail(email : String) : MongoUser?
}