package com.mcu.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.mcu.model.History
import com.mcu.util.AwsConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class HistoryRepository {

    @Autowired
    private lateinit var awsConnector : AwsConnector
    fun save(item: History): History? {
        awsConnector.getDynamoDBMapper().save(item)
        return awsConnector.getDynamoDBMapper().load(item)
    }

    fun getCountByPriorityForReport(priority : String, date : LocalDateTime): Int {
        return awsConnector.getDynamoDBMapper().count(History::class.java, this.getQueryExpressionForReport(priority, date))
    }

    fun getListByPriorityForReport(priority: String, date: LocalDateTime): PaginatedQueryList<History>? {
        return awsConnector.getDynamoDBMapper().query(History::class.java, this.getQueryExpressionForReport(priority, date))
    }

    private fun getQueryExpressionForReport(priority: String, localDate: LocalDateTime): DynamoDBQueryExpression<History> {
        val condition = Condition().withAttributeValueList(AttributeValue()
                .withS(localDate.minusHours(1).toString()))
                .withComparisonOperator(ComparisonOperator.GE)

        return  DynamoDBQueryExpression<History>().withIndexName("History-priority-date")
                        .withConsistentRead(false)
                        .withHashKeyValues(History().also { it.priority = priority })
                        .withRangeKeyCondition("date", condition)
    }
}