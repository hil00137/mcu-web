package com.mcu.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication(exclude = [MongoAutoConfiguration::class, MongoDataAutoConfiguration::class], scanBasePackages = ["com.mcu.*"])
class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
