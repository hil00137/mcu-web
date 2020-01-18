package com.mcu.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication(scanBasePackages = ["com.mcu.*"])
class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
