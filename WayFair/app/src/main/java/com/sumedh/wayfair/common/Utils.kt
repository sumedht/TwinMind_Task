package com.sumedh.wayfair.common
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.round

object Utils {
    fun getReadableDate(input: String): String? {
        val inputDateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            val date = inputDateFormat.parse(input)
            return date?.let { outputDateFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun roundToNearest(value: Double): Int {
        return round(value).toInt()
    }
}