package com.mcu.service

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.Instance
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AwsManagementService {

    @Autowired
    private lateinit var awsConnector: AwsConnector


    fun getInstanceList(instanceIds : List<String>): ArrayList<Instance> {
        val request = DescribeInstancesRequest().withInstanceIds(instanceIds)
        val connection = awsConnector.getConnection()
        val result = connection.describeInstances(request)
        val instanceList = ArrayList<Instance>()
        for (reservation in result.reservations) {
            for (instance in reservation.instances) {
                instanceList.add(instance)
            }
        }
        return instanceList
    }
}