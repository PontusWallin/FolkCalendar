package com.pontuswallin.folkcalendarassignment.model

import com.google.gson.annotations.SerializedName

class HolidaysRequestBody (
    @SerializedName("apiKey") val apiKey : String,
    @SerializedName("startDate") val startDate : String,
    @SerializedName("endDate") val endDate : String
)