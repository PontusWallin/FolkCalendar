package com.pontuswallin.folkcalendarassignment

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pontuswallin.folkcalendarassignment.utilities.HolidayDatesUitilities.Companion.createEmptyHolidayDate
import com.pontuswallin.folkcalendarassignment.utilities.HolidayDatesUitilities.Companion.replace
import com.pontuswallin.folkcalendarassignment.adapters.HolidayRecyclerAdapter
import com.pontuswallin.folkcalendarassignment.database.RealmClient
import com.pontuswallin.folkcalendarassignment.model.HolidayDate
import com.pontuswallin.folkcalendarassignment.model.HolidaysRequestBody
import com.pontuswallin.folkcalendarassignment.networking.RetrofitClient
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.convertToLocalDate
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.getFirstAndLastDateOfWeek
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.getWeekDayArray
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities.Companion.returnDateString
import com.pontuswallin.folkcalendarassignment.utilities.HolidayDatesUitilities
import io.realm.RealmList
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.Period

class MainActivity : AppCompatActivity() {

    private lateinit var realmClient : RealmClient

    private lateinit var recyclerView : RecyclerView
    private lateinit var holidayAdapter : HolidayRecyclerAdapter

    private lateinit var nextWeekBtn : Button
    private lateinit var prevWeekBtn : Button
    private lateinit var weekDaySpinner: Spinner

    private var currentDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initButtons()
        initRecyclerView()
        initSpinner()

        realmClient = RealmClient(applicationContext)
    }

    private fun initRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            holidayAdapter = HolidayRecyclerAdapter()
            adapter = holidayAdapter
        }
    }

    private fun initSpinner() {
        weekDaySpinner = findViewById(R.id.weekDaySpinner)

        weekDaySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            getWeekDayArray())

        weekDaySpinner.setSelection(currentDate.dayOfWeek.value-1)

        weekDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // When the user selects a weekday from the weekday spinner we want to get the
                // difference between the selected weekday and the current weekday
                // this difference is the number of days we need to increment our Current Date with
                // before we tell Realm and/or Retrofit to fetch the new holidays.
                val selectedWeekDayLocal = currentDate.dayOfWeek.value-1
                val daysToIncrement = position - selectedWeekDayLocal
                incrementDaysAndFetchHolidays(daysToIncrement)
            }
        }
    }

    private fun initButtons(){

        nextWeekBtn = findViewById(R.id.nextMonthBtn)
        nextWeekBtn.setOnClickListener {
            incrementDaysAndFetchHolidays(7)
        }

        prevWeekBtn = findViewById(R.id.prevMonthBtn)
        prevWeekBtn.setOnClickListener {
            incrementDaysAndFetchHolidays(-7)
        }
    }

    private fun incrementDaysAndFetchHolidays(daysToAdd: Int) {
        currentDate = currentDate!!.plusDays(daysToAdd.toLong())
        fetchHolidaysForDate()
    }

    private fun fetchHolidaysForDate() {

        // fetch from Realm.
        val holidayDatesFromRealm = fetchFromRealmReturnList()
        // If we recieve less than 7 dates from realm, this means we need to fetch one or more from the API.
        if(holidayDatesFromRealm.size < 7){
            // I use a function to figure out which dates are not cached.
            val missingDates = returnedDatesThatAreMissingForThisWeek(holidayDatesFromRealm)
            // I only need to fetch the ones which are not cached.
            fetchFromAPI(missingDates)
        }
    }

    private fun returnedDatesThatAreMissingForThisWeek(holidayDatesFromRealm: List<HolidayDate>): List<HolidayDate> {
        val emptyHolidayDateList: RealmList<HolidayDate> = RealmList()
        var iDate = currentDate

        // Create a list of empty holidaydates. We will replace these.
        for (i in 1..7) {
            emptyHolidayDateList.add(createEmptyHolidayDate(iDate))
            iDate = iDate.plusDays(1)
        }

        // If we find holidaydate is already cached in realm, we replace it
        holidayDatesFromRealm.forEach { hDate ->
            emptyHolidayDateList.forEach { eDate ->
                if (hDate.date.equals(eDate.date)) {
                    replace(eDate, hDate)
                }
            }
        }
        return emptyHolidayDateList.filter { holidayDate -> !holidayDate.isCached }
    }

    private fun fetchFromAPI(notCachedDates: List<HolidayDate>) {

        // Since Realm only supports the Date-type, and not LocalDate,
        // I have to make a convertion here
        val firstDate = convertToLocalDate(notCachedDates.get(0).date)
        val lastDate = convertToLocalDate(notCachedDates.last().date)

        // Then I have to convert the LocalDates into strings
        // because that's what the API is expecting
        val firstDateAsString = returnDateString(firstDate!!)
        val lastDateAsString = returnDateString(lastDate!!)
        val body = HolidaysRequestBody(RetrofitClient.apiKey, firstDateAsString!!, lastDateAsString!!)

        // I lock the UI, so the user can't make multiple request at once
        lockButtons()

        // And finally I send the GET-request to the server.
        val call: Call<ResponseBody> = RetrofitClient.getClient.getHolidays(body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                // I use a utility class to create a list of days based on the API response.
                val responseAsJSON = responseBodyToJSON(response)
                val daysInRange = Period.between(firstDate, lastDate).days
                val holidaysList = HolidayDatesUitilities.createHolidayDateList(
                    firstDate, daysInRange+1, responseAsJSON
                    )
                // This list is then passed to the updateAppState-method.
                updateAppState(holidaysList)
                // Finally, the UI is unlocked again.
                unlockButtons()
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) { failToast() }
        })
    }

    private fun fetchFromRealmReturnList(): List<HolidayDate> {

        val (firstDate, secondDate) = getFirstAndLastDateOfWeek(currentDate)

        val datesList = realmClient.getHolidayDatesForRange(firstDate, secondDate)

        if(datesList.isNotEmpty()){
            holidayAdapter.submitList(datesList)
            holidayAdapter.notifyDataSetChanged()
        }
        return datesList
    }

    private fun responseBodyToJSON(response : Response<ResponseBody>?): JSONObject {
        val responseAsString = response?.body()?.string()
        val responseASJSON = JSONObject(responseAsString!!)
        return responseASJSON.get("holidays") as JSONObject
    }

    private fun updateAppState(createdFromResponse: RealmList<HolidayDate>) {

        realmClient.update(createdFromResponse)

        val (firstDate, secondDate) = getFirstAndLastDateOfWeek(currentDate)
        val datesList = realmClient.getHolidayDatesForRange(firstDate, secondDate)

        holidayAdapter.submitList(datesList)
        holidayAdapter.notifyDataSetChanged()
    }

    private fun lockButtons(){
        nextWeekBtn.isEnabled = false
        prevWeekBtn.isEnabled = false
    }

    private fun unlockButtons(){
        nextWeekBtn.isEnabled = true
        prevWeekBtn.isEnabled = true
    }

    private fun failToast() {
        Toast.makeText(applicationContext, "Networking failure! - Please try again later!", Toast.LENGTH_LONG).show()
    }
}