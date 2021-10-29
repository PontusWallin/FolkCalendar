package com.pontuswallin.folkcalendarassignment.networking

import com.pontuswallin.folkcalendarassignment.model.HolidaysRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {

    @POST("holidays")
    fun getHolidays(@Body holidaysRequestBody: HolidaysRequestBody):
            Call<ResponseBody>

}
