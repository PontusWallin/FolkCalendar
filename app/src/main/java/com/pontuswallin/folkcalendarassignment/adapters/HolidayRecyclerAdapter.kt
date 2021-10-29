package com.pontuswallin.folkcalendarassignment.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pontuswallin.folkcalendarassignment.R
import com.pontuswallin.folkcalendarassignment.model.HolidayDate
import com.pontuswallin.folkcalendarassignment.model.Holiday
import com.pontuswallin.folkcalendarassignment.utilities.DateUtilities
import io.realm.RealmList
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class HolidayRecyclerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var items : List<HolidayDate> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HolidayViewHolder (
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.layout_date_list_item, parent, false)
            , parent.context
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){ is HolidayViewHolder -> {
            holder.bind(items.get(position))
        }}
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        if( holder is HolidayViewHolder) {
            holder.clearHolidaysLayout()
        }
    }

    fun submitList(holidays: List<HolidayDate>) {
        items = holidays
    }

    class HolidayViewHolder constructor(
        itemView: View,
        private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        private val dateCard: CardView = itemView.findViewById(R.id.dateCard)
        private val holidaysLayout: LinearLayout = itemView.findViewById(R.id.holidaysLayout)
        private val dateTextview : TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(holidayDate: HolidayDate) {

            // Background color should he white, if no holiday exists for this day.
            dateCard.setCardBackgroundColor(Color.WHITE)

            // We need to display a user friendly and easily readable date string.
            val ld = DateUtilities.convertToLocalDate(holidayDate.date)
            val dateDisplayString = ld?.format(DateTimeFormatter.ofPattern("EEEE dd-MMM"))
            dateTextview.text = dateDisplayString

            if(holidayDate.holidays!!.size > 0) {
                // if there are holidays present, we change the background color.
                dateCard.setCardBackgroundColor(Color.LTGRAY)
                bindHolidays(holidayDate.holidays)
            } else {
                // there are no holidays present, the text on the card should just say "No Holiday Today"
                setNoHolidayText()
            }
        }

        private fun setNoHolidayText() {
            val singleHolidayLayout = LinearLayout(context)
            singleHolidayLayout.orientation = LinearLayout.VERTICAL

            // create name textview
            val tv_holiday_name = TextView(context)
            tv_holiday_name.textSize = 14f
            tv_holiday_name.text = "No Holiday today!"
            tv_holiday_name.gravity = Gravity.CENTER

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            tv_holiday_name.layoutParams = params

            singleHolidayLayout.addView(tv_holiday_name)

            holidaysLayout.addView(singleHolidayLayout)
        }

        private fun bindHolidays(holidays: RealmList<Holiday>?) {

            holidays?.forEach { holiday ->
                // create horizontal layout
                val singleHolidayLayout = LinearLayout(context)
                singleHolidayLayout.orientation = LinearLayout.VERTICAL

                // create name textview
                val tv_holiday_name = TextView(context)
                tv_holiday_name.textSize = 20f


                if(holiday.type == "folk") {
                    decorateTextForFolkHoliday(tv_holiday_name)
                } else {
                    decorateTextForPublicHoliday(tv_holiday_name)
                }

                val holidayTypeCapitalized: String = holiday.type.substring(0, 1)
                    .uppercase(Locale.getDefault()) + holiday.type.substring(1)
                tv_holiday_name.text = holiday.name + "(" + holidayTypeCapitalized + " holiday)"
                tv_holiday_name.gravity = Gravity.CENTER

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )

                tv_holiday_name.layoutParams = params

                singleHolidayLayout.addView(tv_holiday_name)

                holidaysLayout.addView(singleHolidayLayout)
            }
        }

        private fun decorateTextForPublicHoliday(tv_holiday_name: TextView) {
            tv_holiday_name.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC)
            tv_holiday_name.setTextColor(Color.RED)
        }

        private fun decorateTextForFolkHoliday(tv_holiday_name: TextView) {
            tv_holiday_name.setTypeface(Typeface.SANS_SERIF)
            tv_holiday_name.setTextColor(Color.BLUE)
        }

        fun clearHolidaysLayout() {
            holidaysLayout.removeAllViews();
        }
    }
}