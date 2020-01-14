package com.mcu.configuration

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class DynamoDbConfig {
    class LocalDateTimeConverter : DynamoDBTypeConverter<Date, LocalDateTime> {
        override fun unconvert(source: Date?): LocalDateTime? {
            return source?.toInstant()?.atZone(TimeZone.getDefault().toZoneId())?.toLocalDateTime()
        }

        override fun convert(source: LocalDateTime?): Date {
            return Date.from(source?.atZone(ZoneId.systemDefault())!!.toInstant())
        }
    }
}