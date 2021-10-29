package com.pontuswallin.folkcalendarassignment.utilities

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*

class DateUtilities {


    companion object {

        fun returnDateString(dateToFormat: LocalDate): String? {
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return dateFormat.format(dateToFormat)
        }

        fun getFirstAndLastDateOfWeek(date: LocalDate): Pair<LocalDate, LocalDate> {

            val lastWeekDayDate = date.plusDays(6)
            return Pair (date, lastWeekDayDate)
        }


        fun createSimpleDateString(localDate: LocalDate?) : String {
            val dayWithZeroPrefix = addZeroPrefix(localDate?.dayOfMonth.toString())
            val monthWithZeroPrefix = addZeroPrefix(localDate?.monthValue.toString())
            return "${localDate?.year}-${monthWithZeroPrefix}-${dayWithZeroPrefix}"
        }
        fun dateStringToDateObj(date: String): Date {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            return dateFormat.parse(date)
        }

        fun getWeekDayArray() =
            arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        fun convertToLocalDate(dateToConvert: Date): LocalDate? {
            return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        fun convertToDate(dateToConvert: LocalDate): Date? {
            return Date.from(
                dateToConvert.atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
        }

        fun addZeroPrefix(day: String): String {

            if(day.toInt() <10) {
                return "0"+day
            }
            return day

        }
    }
}