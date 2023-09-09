package vn.techres.line.helper

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeFormatHelper {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    const val TIME = "dd/MM/yyyy HH:mm"
    fun timeAgoString(date: String?): String {
        try {
            if(!date.isNullOrBlank()){
                val past = getDateFromString(date)
                val now = Date()
                val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past!!.time)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
                val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
                val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)
                return when {
                    seconds < 60 -> {
                        String.format("%s", "Vừa xong")
                    }
                    minutes < 60 -> {
                        String.format("%d phút", minutes)
                    }
                    hours < 24 -> {
                        String.format("%d giờ", hours)
                    }
                    else -> {
                        String.format("%d ngày", days)
                    }
                }
            }
        } catch (j: Exception) {
            j.printStackTrace()
        }
        return String.format("%d giây trước", 1)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFromString(strDate: String?): Date? {
        val dateFormat = SimpleDateFormat(TIME)
        var date: Date? = null
        try {
            if(!strDate.isNullOrBlank()){
                date = dateFormat.parse(strDate)
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun timeGroupAgoString(context: Context, date: String?): String {
        try {
            if(!date.isNullOrBlank()){
                val past = getDateFromString(date)
                val now = Date()
                val dayOfTheWeek = DateFormat.format("EEEE", past) as String // Thursday
                val day = DateFormat.format("dd", past) as String // 20


                val monthNumber = DateFormat.format("MM", past) as String // 06
                val monthCurrent = DateFormat.format("MM", now) as String // 06

                val year = DateFormat.format("yyyy", past) as String // 2013

                val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past!!.time)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
                val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

                return when {
                    seconds < 60 -> {
                        String.format("%s", "Vừa xong")
                    }
                    minutes < 60 -> {
                        String.format("%d phút", minutes)
                    }
                    hours < 24 -> {
                        String.format("%d giờ", hours)
                    }
                    hours < 48 ->{
                        "Hôm qua"
                    }
                    getDayOfWeek(now) == getDayOfWeek(past) && monthNumber == monthCurrent->{
                        dayOfTheWeek
                    }
                    getDayOfWeek(now) != getDayOfWeek(past) && monthNumber == monthCurrent ->{
                        String.format("%d/%d", day, monthNumber)
                    }
                    else -> {
                        String.format("%d/%d/%d", day, monthNumber, year)
                    }
                }
            }
        } catch (j: Exception) {
            j.printStackTrace()
        }
        return String.format("%d giây trước", 1)
    }

    private fun getDayOfWeek(date: Date): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.DAY_OF_WEEK]
    }

    private fun getDayOfTheWeek(date : String) : String{
        return when(date){
            "Monday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.monday_vn)
            }
            "Tuesday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.tuesday_vn)
            }
            "Wednesday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.wednesday_vn)
            }
            "Thursday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.thursday_vn)
            }
            "Friday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.friday_vn)
            }
            "Saturday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.saturday_vn)
            }
            "Sunday" ->{
                TechResApplication.applicationContext().resources.getString(R.string.sunday_vn)
            }
            else ->{
                TechResApplication.applicationContext().resources.getString(R.string.monday_vn)
            }
        }
    }
    
    fun getTimeAgo(date: Date): String {
        var time = date.time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = date.time
        if (time > now || time <= 0) {
            return "in the future"
        }
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> {
                "moments ago"
            }
            diff < 2 * MINUTE_MILLIS -> {
                "a minute ago"
            }
            diff < 60 * MINUTE_MILLIS -> {
                String.format("%d", diff / HOUR_MILLIS)+ " minutes ago"
            }
            diff < 2 * HOUR_MILLIS -> {
                "an hour ago"
            }
            diff < 24 * HOUR_MILLIS -> {
                String.format("%d", diff / HOUR_MILLIS)+ " hours ago"
            }
            diff < 48 * HOUR_MILLIS -> {
                "yesterday"
            }
            else -> {
                String.format("%d", diff / HOUR_MILLIS) + " days ago"
            }
        }
    }

    fun formatTime(hour: Int, minutes: Int): String {
        var strHour = hour.toString() + ""
        var strMinute = minutes.toString() + ""
        if (hour < 10) strHour = "0$strHour"
        if (minutes < 10) strMinute = "0$strMinute"
        return "$strHour:$strMinute"
    }

    fun convertMinuteToHour(minute: Int): String {
        return if (minute != 0) {
            val hour = minute / 60
            val min = minute - hour * 60
            if (hour > 0) {
                hour.toString() + " h " + min + "p"
            } else {
                min.toString() + "p"
            }
        } else ""
    }

    fun convertMillieToHMmSs(millie: Long): String {
        val seconds = millie / 1000
        val second = seconds % 60
        val minute = seconds / 60 % 60
        val hour = seconds / (60 * 60) % 24

        return if (hour > 0) {
            String.format("%02d giờ %02d phút %02d giây", hour, minute, second)
        } else {
            if(minute > 0){
                String.format("%02d phút %02d giây", minute, second)
            }else{
                String.format("%02d giây", second)
            }
        }
    }
    fun getTime(strDate: String?): String? {
        try {
            if (StringUtils.isNullOrEmpty(strDate)) {
                return ""
            }
            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(TIME)
            return sdf.format(strDate)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }
    @SuppressLint("SimpleDateFormat")
    fun getTimeListGame(strDate: String?): String?{
        try {
            if (StringUtils.isNullOrEmpty(strDate)) {
                return ""
            }
            val sdf = SimpleDateFormat(TIME)
            val newDate = sdf.parse(strDate!!)
            val hourFormat = SimpleDateFormat("HH")
            val minuteFormat = SimpleDateFormat("mm")
            val secondFormat = SimpleDateFormat("ss")
            if(newDate == null){
                return ""
            }
            return String.format("Giờ %s phút %s giây %s", hourFormat.format(newDate), minuteFormat.format(newDate), secondFormat.format(newDate))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(strDate: String?): String? {
        try {
            if (StringUtils.isNullOrEmpty(strDate)) {
                return ""
            }
             val sdf = SimpleDateFormat(TIME)
            val newDate = sdf.parse(strDate!!)
            val dayFormat = SimpleDateFormat("dd")
            val monthFormat = SimpleDateFormat("MM")
//            val yearFormat = SimpleDateFormat("yyyy")
            if(newDate == null){
                return ""
            }
            return String.format("Ngày %s Tháng %", dayFormat.format(newDate), monthFormat.format(newDate))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFromStringDayMonthYear(strDate: String?): String {
        try {
            if (StringUtils.isNullOrEmpty(strDate)) {
                return ""
            }
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val newDate = format.parse(strDate!!)
            val dayFormat = SimpleDateFormat("dd/MM/yyyy")
            val timeFormat = SimpleDateFormat("HH:mm")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentTime = sdf.format(Date())
            if(newDate == null){
                return ""
            }
            return if (currentTime == dayFormat.format(newDate)) {
                java.lang.String.format(
                    "%s%s%s",
                    timeFormat.format(newDate),
                    ", ",
                    Utils.getString(R.string.today)
                )
            }else{
                String.format(
                    "%s%s%s",
                    timeFormat.format(newDate),
                    ", ",
                    dayFormat.format(newDate)
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFromStringDay(strDate: String?): String {
        try {
            if (StringUtils.isNullOrEmpty(strDate)) {
                return ""
            }
            var format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val newDate = format.parse(strDate!!)
            format = SimpleDateFormat("HH:mm")
            return format.format(newDate!!)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    fun calculateDuration(duration: Int): String {
        var finalDuration = ""
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong())

        if (minutes == 0L) {
            finalDuration = if(seconds < 10){
                "0:0$seconds"
            }else{
                "0:$seconds"
            }
        } else {
            if (seconds >= 60) {
                val sec = seconds - minutes * 60
                finalDuration = if(sec < 10){
                    "$minutes:0$sec"
                }else{
                    "$minutes:$sec"
                }
            }
        }
        return finalDuration
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeNow(date: Date?) : String?{
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss a")
        return df.format(date?.time)
    }
}