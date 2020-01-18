package com.mcu.configuration

import com.mongodb.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate


//@Configuration
//@EnableMongoRepositories(basePackages = ["com.mcu.repository"], mongoTemplateRef = "mongoTemplate", includeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*ServerRepository"])])
class MongoConfig {

    @Value("\${spring.data.mongodb.host}")
    lateinit var host : String

    @Value("\${spring.data.mongodb.database}")
    lateinit var databaseName : String

    @Value("\${spring.data.mongodb.username}")
    lateinit var username : String

    @Value("\${spring.data.mongodb.password}")
    lateinit var password : String

    /**
     * Required username, password, mechanism
     */
//    @Bean
    fun mongoClient() : MongoClient {
        val credential = MongoCredential.createCredential(username, databaseName, password.toCharArray()).withMechanism(AuthenticationMechanism.SCRAM_SHA_1)
        val options = MongoClientOptions.builder().connectionsPerHost(30).minConnectionsPerHost(10).build()
        return MongoClient(ServerAddress(host,27017), credential,options)
    }

//    @Primary
//    @Bean
    fun mongoTemplate() = MongoTemplate(mongoClient(), databaseName)
}