package com.mcu.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user")
class User {
    var userId : String = ""
    var password : String = ""
    var auth : String = "common"
}