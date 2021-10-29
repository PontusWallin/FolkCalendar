package com.pontuswallin.folkcalendarassignment.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDate
import java.util.*

open class HolidayDate(
    @PrimaryKey
    var id: UUID = UUID.randomUUID(),
    var isCached: Boolean = false,
    var date: Date = Date(),
    var holidays: RealmList<Holiday>? = null,
): RealmObject()