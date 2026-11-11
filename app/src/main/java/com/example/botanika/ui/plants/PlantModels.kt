package com.example.botanika.ui.plants

import com.example.botanika.R
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.daysBetween
import com.example.botanika.ui.components.formatDate
import com.example.botanika.ui.components.formatTime
import com.example.botanika.ui.home.CareTask
import com.example.botanika.ui.home.CareType

enum class PlantLocation(val label: Int) {
    Living(R.string.location_living),
    Bedroom(R.string.location_bedroom),
    Kitchen(R.string.location_kitchen),
    Balcony(R.string.location_balcony),
    Garden(R.string.location_garden),
    Office(R.string.location_office)
}

data class MyPlant(
    val id: String,
    val catalogId: String?,
    val name: String,
    val image: PlantImage,
    val location: PlantLocation,
    val wateringEveryDays: Int,
    val lastWateredUtc: Long,
    val notes: String
) {
    val nextWateringUtc: Long get() = addDays(lastWateredUtc, wateringEveryDays)
}

data class PlantDraft(
    val catalogId: String?,
    val name: String?,
    val image: PlantImage
)

enum class RepeatOption(val days: Int, val label: Int) {
    Never(0, R.string.repeat_never),
    Daily(1, R.string.repeat_daily),
    Weekly(7, R.string.repeat_weekly),
    Biweekly(15, R.string.repeat_biweekly),
    Monthly(30, R.string.repeat_monthly)
}

data class Reminder(
    val id: String,
    val plantId: String,
    val plantName: String,
    val type: CareType,
    val dateUtc: Long,
    val hour: Int,
    val minute: Int,
    val repeatDays: Int,
    val note: String,
    val notify: Boolean
)

fun Reminder.occursOn(dayUtc: Long): Boolean {
    val diff = daysBetween(dateUtc, dayUtc)
    return diff == 0 || (repeatDays > 0 && diff > 0 && diff % repeatDays == 0)
}

fun Reminder.nextOccurrence(todayUtc: Long): Long? {
    val diff = daysBetween(dateUtc, todayUtc)
    return when {
        diff <= 0 -> dateUtc
        repeatDays <= 0 -> null
        else -> addDays(dateUtc, ((diff + repeatDays - 1) / repeatDays) * repeatDays)
    }
}

fun Reminder.toCareTask(onDayUtc: Long): CareTask = CareTask(
    plant = plantName,
    type = type,
    date = "${formatDate(onDayUtc)} · ${formatTime(hour, minute)}",
    id = id,
    notify = notify
)

val CareType.label: Int
    get() = when (this) {
        CareType.Water -> R.string.care_type_water
        CareType.Sun -> R.string.care_type_sun
        CareType.Pesticide -> R.string.care_type_pesticide
        CareType.Fertilizer -> R.string.care_type_fertilizer
    }
