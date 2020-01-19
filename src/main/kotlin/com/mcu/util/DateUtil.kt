package com.mcu.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateUtil {
    companion object {
        /**
         * 오늘 날짜의 경우 시간과 분. 그외의 날짜의 경우 일자만.
         */
        fun transform(dateTime : LocalDateTime): String {
            val today = LocalDate.now()
            if(dateTime.toLocalDate().isEqual(today)) {
                return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH시 mm분"))
            }
            return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        }

        fun transMailFormYmdh(dateTime: LocalDateTime) : String {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시"))
        }

        fun transMailFormHms(dateTime: LocalDateTime) : String {
            return dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초"))
        }
    }
}