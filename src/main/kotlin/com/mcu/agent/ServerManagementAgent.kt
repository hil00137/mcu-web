package com.mcu.agent

import com.amazonaws.services.ec2.model.Instance
import com.mcu.model.Server
import com.mcu.service.AwsManagementService
import com.mcu.service.HistoryService
import com.mcu.service.McuServerManagementService
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class ServerManagementAgent {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    private lateinit var executorService : ExecutorService

    @Autowired
    private lateinit var awsManagementService : AwsManagementService

    @Autowired
    private lateinit var mcuServerManagementService : McuServerManagementService

    @Autowired
    private lateinit var historyService: HistoryService

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
                        val aws = mcuServer.aws
                        awsIdMap[aws.awsId?:continue] = mcuServer
                    }
                    var active = 0
                    var total = 0
                    val instanceList = awsManagementService.getInstanceList(awsIdMap.keys().toList())
                    for (instance in instanceList) {
                        val server = this.awsUpdate(instance, awsIdMap[instance.instanceId])
                        if (instance.state.code == 16) {
                            active++
                            val mcProperty = this.getMinecraftServerStatus(instance.publicIpAddress)
                            this.mcUpdate(server, mcProperty)
                        }
                        total++
                    }

                    logger.info("Active Aws Server $active / $total")
                    Thread.sleep(5000)
                }

            } catch (e : Exception) {
                logger.error(e.message)
            }
        }

        executorService.shutdown()
    }

    private fun awsUpdate(instance: Instance, server: Server?): Server? {
        if (server == null) return null

        when (instance.state.code) {
            16 -> {
                server.aws.online = true
                server.aws.start = LocalDateTime.ofInstant(instance.launchTime.toInstant(), ZoneId.systemDefault())
                server.ip = instance.publicIpAddress
            }
            else -> {
                server.aws.online = false
                server.minecraft.online = false
                server.aws.start = null
                server.ip = null.toString()
            }
        }
        server.aws.code = instance.state.code
        server.aws.update = LocalDateTime.now()
        return mcuServerManagementService.updateProperty(server)
    }

    @PreDestroy
    fun destroy() {
        executorService.shutdownNow()
    }

    private fun mcUpdate(server: Server?, property : List<String>) {
        if (server == null) {
            return
        }
        if (property.isNotEmpty()) {
            server.minecraft.online = true
            server.minecraft.now = property[2].toInt()
            server.minecraft.max = property[3].toInt()
            server.minecraft.update = LocalDateTime.now()
            if(server.minecraft.zeroTime == null && server.minecraft.now == 0) {
                server.minecraft.zeroTime = LocalDateTime.now()
            } else if(server.minecraft.now != 0){
                server.minecraft.zeroTime = null
            } else if(server.minecraft.zeroTime != null) {
                val gap= ChronoUnit.MINUTES.between(server.minecraft.zeroTime, server.minecraft.update)
                if (gap >= 3) {
                    logger.info("Server Shutdown")
                    awsManagementService.stopInstance(server.aws.awsId.toString())
                    historyService.writeHistoryAsSystem("Server Shutdown")
                }
            }
        } else {
            server.minecraft.online = false
            server.minecraft.now = 0
            server.minecraft.max = 0
            server.minecraft.zeroTime = null
            server.minecraft.update = LocalDateTime.now()
        }
        mcuServerManagementService.updateProperty(server)
    }

    private fun getMinecraftServerStatus(ip : String): List<String> {
        var socket : Socket? = null
        try {
            socket = Socket()
            socket.soTimeout = 3000
            val socketAddress = InetSocketAddress(ip, 25565)
            socket.connect(socketAddress, 3000)
            val outStream = socket.getOutputStream()
            val dout = DataOutputStream(outStream)
            val inStream = socket.getInputStream()
            val dis = BufferedReader(inStream.bufferedReader(Charsets.UTF_8))
            dout.write(254)
            val sb = StringBuffer()
            while(true) {
                val char = dis.read()
                if (char == -1) {
                    break
                }
                if (char != 0 && char > 16 && char != 255 && char != 23 && char != 24) {
                    sb.append(char.toChar())
                }
            }
            socket.close()
            return sb.toString().split("�")
        } catch (e: Exception) {
            logger.info("Minecraft Sever is not ready")
        } finally {
            socket?.close()
        }
        return ArrayList()
    }
}