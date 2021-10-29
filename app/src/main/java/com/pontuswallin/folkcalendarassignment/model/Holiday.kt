package com.pontuswallin.folkcalendarassignment.model

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Holiday (

	var date : String = "",
	var name : String = "",
	var type : String = ""
): RealmObject()