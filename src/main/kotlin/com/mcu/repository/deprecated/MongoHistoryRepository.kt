package com.mcu.repository.deprecated

import com.mcu.model.deprecated.MongoHistory
import org.springframework.data.mongodb.repository.MongoRepository

@Deprecated("migration complete")
interface MongoHistoryRepository : MongoRepository<MongoHistory, String> {

}