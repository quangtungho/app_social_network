package vn.techres.line.bindingAdapter

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import vn.techres.line.helper.TimeFormatHelper

@BindingAdapter(
    "intervalOfTime",
    "today"
)
fun TextView.intervalOfTime(intervalOfTime : String?, today : Int?){
    if(today ?: 0 == 1){
        visibility = View.VISIBLE
        text = TimeFormatHelper.getDateFromStringDayMonthYear(intervalOfTime ?: "")
    }else visibility = View.GONE
}

@BindingAdapter("timeMessage")
fun TextView.timeMessage(timeMessage : String?){
    text = timeMessage ?: ""
}