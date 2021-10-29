package com.pontuswallin.folkcalendarassignment.utilities

import com.pontuswallin.folkcalendarassignment.model.Holiday
import com.pontuswallin.folkcalendarassignment.model.HolidayDate
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.convertToDate
import io.realm.RealmList
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.*

class HolidayDatesUitilities {

    companion object {
        private var currentDateString = ""

        private lateinit var holidaysJSON: JSONObject
        private var datesList = RealmList<HolidayDate>()
        private lateinit var currentDate: LocalDate
        fun createHolidayDateList(
            cdate: LocalDate,
            daysInRange: Int,
            jsonObject: JSONObject): RealmList<HolidayDate> {

            datesList = RealmList()
            currentDate = cdate
            holidaysJSON = jsonObject


            for (i in 0 until daysInRange) {
                createHolidayDate()
            }
            return datesList
        }

        private fun createHolidayDate() {
            // First we create all Dates with holidays...
            val thisDayHasAHoliday = createDayWithHolidays()
            // ... after  we have to create ates without holidays
            // Since the API doesn't give us these.
            if (!thisDayHasAHoliday) {
                createDayWithoutHoliday()
            }
            // Lastly, we add a day to the current date
            // So the loop will create the next holiday date for us
            currentDate = currentDate.plusDays(1)
        }

        private fun createDayWithHolidays(): Boolean {

            // The keys in this JSON object are the dates which are holidays
            val holidayDates = holidaysJSON.keys()
                .asSequence()
                .toList()

            currentDateString = DateUtilities.createSimpleDateString(currentDate)
            var holidayDateAdded = false
            var cDate = currentDateString
            holidayDates.forEach { holidayDate ->
                if (holidayDate == currentDateString) {
                    addHolidayDateToDateList()
                    holidayDateAdded = true
                }
            }
            return holidayDateAdded
        }

        private fun addHolidayDateToDateList() {
            val holidaysInCurrentDate: JSONArray =
                holidaysJSON.get(currentDateString) as JSONArray
            val holidays = createHolidaysList(holidaysInCurrentDate)

            datesList.add(HolidayDate(
                UUID.randomUUID(), true, convertToDate(currentDate)!!, holidays
            ))
        }

        private fun createHolidaysList(holidaysInCurrentDate: JSONArray): RealmList<Holiday> {

            val holidays = RealmList<Holiday>()
            for (i in 0 until holidaysInCurrentDate.length()) {
                val holidayJSON = holidaysInCurrentDate.getJSONObject(i)
                val name = holidayJSON.get("name") as String
                val type = holidayJSON.get("type") as String

                val holiday = Holiday(currentDateString, name, type)
                holidays += holiday
            }
            return holidays
        }

        private fun createDayWithoutHoliday() {
            val dateObj = DateUtilities.dateStringToDateObj(currentDateString)
            datesList.add(HolidayDate(UUID.randomUUID(), true, dateObj, RealmList()))
        }

        fun replace(eDate: HolidayDate, hDate: HolidayDate) {
            eDate.id = hDate.id
            eDate.holidays = hDate.holidays
            eDate.isCached = true
        }

        fun createEmptyHolidayDate(iDate: LocalDate) = HolidayDate(
            UUID.randomUUID(), false, convertToDate(iDate)!!, RealmList()
        )
    }
}