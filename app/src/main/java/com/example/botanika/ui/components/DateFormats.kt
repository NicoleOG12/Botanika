package com.example.botanika.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Utc = TimeZone.getTimeZone("UTC")
private val BrazilLocale = Locale.forLanguageTag("pt-BR")
private const val DAY_MILLIS = 24L * 60 * 60 * 1000

fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(Utc).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun addDays(utcMillis: Long, days: Int): Long = utcMillis + days * DAY_MILLIS

fun daysBetween(from: Long, to: Long): Int = ((to - from) / DAY_MILLIS).toInt()

fun utcMillisOf(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance(Utc).apply { clear(); set(year, month - 1, day) }.timeInMillis

fun utcYear(utcMillis: Long): Int = Calendar.getInstance(Utc).apply { timeInMillis = utcMillis }.get(Calendar.YEAR)

fun utcMonth(utcMillis: Long): Int =
    Calendar.getInstance(Utc).apply { timeInMillis = utcMillis }.get(Calendar.MONTH) + 1

fun utcDay(utcMillis: Long): Int =
    Calendar.getInstance(Utc).apply { timeInMillis = utcMillis }.get(Calendar.DAY_OF_MONTH)

fun formatDayMonth(utcMillis: Long): String =
    SimpleDateFormat("dd/MM", BrazilLocale).apply { timeZone = Utc }.format(Date(utcMillis))

fun formatDate(utcMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", BrazilLocale).apply { timeZone = Utc }.format(Date(utcMillis))

fun formatMonthYear(utcMillis: Long): String =
    SimpleDateFormat("MMMM 'de' yyyy", BrazilLocale).apply { timeZone = Utc }.format(Date(utcMillis))

fun formatTime(hour: Int, minute: Int): String = String.format(BrazilLocale, "%02d:%02d", hour, minute)
