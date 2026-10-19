package com.example.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val argFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun todayIso(): String {
        return isoFormat.format(Date())
    }

    fun todayArg(): String {
        return argFormat.format(Date())
    }

    /**
     * Converts YYYY-MM-DD to DD/MM/AAAA
     */
    fun toArgFormat(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "-"
        return try {
            if (isoDate.contains("/")) {
                isoDate
            } else {
                val d = isoFormat.parse(isoDate)
                if (d != null) argFormat.format(d) else isoDate
            }
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Converts DD/MM/AAAA to YYYY-MM-DD
     */
    fun toIsoFormat(argDate: String?): String {
        if (argDate.isNullOrBlank()) return ""
        return try {
            if (argDate.contains("-")) {
                argDate
            } else {
                val d = argFormat.parse(argDate)
                if (d != null) isoFormat.format(d) else argDate
            }
        } catch (e: Exception) {
            argDate
        }
    }

    /**
     * Returns days until target date.
     * Negative means expired. 0 means today. Positive means in the future.
     */
    fun daysUntil(targetDateStr: String?): Long? {
        if (targetDateStr.isNullOrBlank()) return null
        return try {
            val date = if (targetDateStr.contains("/")) {
                argFormat.parse(targetDateStr)
            } else {
                isoFormat.parse(targetDateStr)
            } ?: return null

            // Clean time to midnight
            val calTarget = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = calTarget.timeInMillis - calToday.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMillis)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns a human readable status in Spanish for days remaining
     */
    fun describeExpiry(days: Long?): String {
        if (days == null) return "Sin fecha"
        return when {
            days < -1 -> "Vencido hace ${kotlin.math.abs(days)} días"
            days == -1L -> "Venció ayer"
            days == 0L -> "Vence HOY"
            days == 1L -> "Vence mañana"
            days in 2..7 -> "Vence en $days días (Esta semana)"
            days in 8..15 -> "Vence en $days días (Próximos 15 días)"
            days in 16..30 -> "Vence en $days días (Próximos 30 días)"
            else -> "Vence en $days días"
        }
    }

    /**
     * Adds specified days or months to a date (default 1 year for annual renewal)
     */
    fun addOneYear(isoDate: String): String {
        return try {
            val d = isoFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = d
                add(Calendar.YEAR, 1)
            }
            isoFormat.format(cal.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun addMonths(isoDate: String, months: Int): String {
        return try {
            val d = isoFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = d
                add(Calendar.MONTH, months)
            }
            isoFormat.format(cal.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun currentTimestampFormatted(): String {
        return displayDateTimeFormat.format(Date())
    }
}
