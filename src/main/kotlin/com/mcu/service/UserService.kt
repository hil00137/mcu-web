package com.mcu.service

import com.mcu.model.User
import com.mcu.repository.UserRepository
import com.mcu.util.HashUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    private lateinit var userRepository : UserRepository

    @Cacheable(value = ["userCache"], key = "'userId:' + #userId")
    fun getUserById(userId : String) = userRepository.findUserByUserId(userId)
    @Cacheable(value = ["userCache"], key = "'nickname:' + #nickname")
    fun getUserByNickname(nickname : String) = userRepository.findUserByNickname(nickname)
    @CacheEvict(value = ["userCache"], allEntries = true)
    fun registerUser(user: User): User? {
        user.password = HashUtil.sha512(user.password)
        return userRepository.save(user)
    }
}