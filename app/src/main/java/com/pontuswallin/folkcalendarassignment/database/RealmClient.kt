package com.pontuswallin.folkcalendarassignment.database

import android.content.Context
import com.pontuswallin.folkcalendarassignment.model.HolidayDate
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.convertToDate
import io.realm.*
import io.realm.kotlin.where
import java.time.LocalDate

class RealmClient(context : Context) {

    var realm : Realm
    lateinit var context : Context

    init{

        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
       realm = Realm.getInstance(config)
    }

    fun update(datesList : RealmList<HolidayDate>) {

        realm.beginTransaction()
        realm.copyToRealmOrUpdate(datesList)
        realm.commitTransaction()
    }

    fun getHolidayDatesForRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDate> {

        var debug = realm.where<HolidayDate>()
            .between(
                "date",
                convertToDate(startDate),
                convertToDate(endDate)
            ).findAll().sort("date")

        return realm.copyFromRealm(debug)
    }

    fun clearDB() {
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
    }
}