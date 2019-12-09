package com.mcu.repository

import com.mcu.model.History
import org.springframework.data.mongodb.repository.MongoRepository

interface HistoryRepository : MongoRepository<History, String> {

}