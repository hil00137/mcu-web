package com.mcu.service

import com.mcu.model.User
import com.mcu.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    private lateinit var userRepository : UserRepository


    fun getUserById(userId : String) : User? {
        return userRepository.findUserByUserId(userId)
    }
}