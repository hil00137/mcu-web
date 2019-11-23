package com.mcu.agent

import com.amazonaws.services.ec2.model.Instance
import com.mcu.model.Server
import com.mcu.service.AwsManagementService
import com.mcu.service.McuServerManagementService
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class ServerManagementAgent {

    private lateinit var executorService : ExecutorService

    @Autowired
    private lateinit var awsManagementService : AwsManagementService

    @Autowired
    private lateinit var mcuServerManagementService : McuServerManagementService

    @PostConstruct
    fun start() {
        val factory = BasicThreadFactory.Builder().namingPattern("server-manager-thread-%d").build()
        executorService = Executors.newSingleThreadExecutor(factory)
        executorService.execute {
            try {
                while(true) {
                    val awsIdMap = ConcurrentHashMap<String, Server>()
                    val mcuServerList = mcuServerManagementService.getAllMcuServerList()
                    for (mcuServer in mcuServerList) {
                        awsIdMap[mcuServer.aws.id?:continue] = mcuServer
                    }
                    var active = 0
                    var total = 0
                    val instanceList = awsManagementService.getInstanceList(awsIdMap.keys().toList())
                    for (instance in instanceList) {
                        this.awsUpdate(instance, awsIdMap[instance.instanceId])
                        if (instance.state.code == 16) active++
                        total++
                    }

                    println("Active Aws Server $active / $total")
                    Thread.sleep(5000)
                }

            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        executorService.shutdown()
    }

    private fun awsUpdate(instance: Instance, server: Server?) {
        if (server == null) return

        when (instance.state.code) {
            16 -> {
                server.aws.online = true
                server.aws.start = instance.launchTime
                server.ip = instance.publicIpAddress
            }
            else -> {
                server.aws.online = false
                server.aws.start = null
                server.ip = null.toString()
            }
        }
        server.aws.code = instance.state.code
        server.aws.update = Calendar.getInstance().time
        mcuServerManagementService.updateProperty(server)
    }

    @PreDestroy
    fun destroy() {
        executorService.shutdownNow()
    }
}