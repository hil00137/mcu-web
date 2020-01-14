package com.mcu.service

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.StartInstancesRequest
import com.amazonaws.services.ec2.model.StopInstancesRequest
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AwsManagementService {

    @Autowired
    private lateinit var awsConnector: AwsConnector


    fun getInstanceList(instanceIds : List<String>): ArrayList<Instance> {
        val request = DescribeInstancesRequest().withInstanceIds(instanceIds)
        val connection = awsConnector.getEC2Connection()
        val result = connection.describeInstances(request)
        val instanceList = ArrayList<Instance>()
        for (reservation in result.reservations) {
            for (instance in reservation.instances) {
                instanceList.add(instance)
            }
        }
        return instanceList
    }

    fun startInstance(instanceId : String): Int {
        val request = StartInstancesRequest().withInstanceIds(instanceId)
        val connection = awsConnector.getEC2Connection()
        val result = connection.startInstances(request)
        return result.startingInstances.size
    }

    fun stopInstance(instanceId : String) : Int {
        val request = StopInstancesRequest().withInstanceIds(instanceId)
        val connection = awsConnector.getEC2Connection()
        val result = connection.stopInstances(request)
        return result.stoppingInstances.size
    }
}