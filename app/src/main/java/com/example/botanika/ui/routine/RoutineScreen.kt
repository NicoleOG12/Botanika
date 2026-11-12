package com.example.botanika.ui.routine

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.BottomNavBar
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.gardenBackdrop
import com.example.botanika.ui.components.PlantScreenHeader
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.formatDayMonth
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.components.utcDay
import com.example.botanika.ui.components.utcMillisOf
import com.example.botanika.ui.components.utcMonth
import com.example.botanika.ui.components.utcYear
import com.example.botanika.ui.home.CareTaskCard
import com.example.botanika.ui.plants.Reminder
import com.example.botanika.ui.plants.occursOn
import com.example.botanika.ui.plants.toCareTask
import com.example.botanika.ui.theme.BotanikaBackground
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.plants.isFuture
import com.example.botanika.ui.theme.BotanikaSage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private val SelectionPink = BotanikaPink
private val NextMonthGray = Color(0xFFB8B8B8)

private const val CONTENT_STAGGER_MILLIS = 110L
private const val CONTENT_FADE_MILLIS = 700
private const val MAX_ANIMATED_TASKS = 12

private val MonthNames = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)

private val Weekdays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

@Composable
internal fun RoutineScreen(
    reminders: List<Reminder>,
    onNewReminder: () -> Unit = {},
    onEditReminder: (Reminder) -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onTabSelected: (MainTab) -> Unit = {}
) {
    val futureReminders = remember(reminders) {
        reminders.filter { it.isFuture() }
    }
    val today = remember { todayUtcMillis() }
    var currentYear by rememberSaveable { mutableIntStateOf(utcYear(today)) }
    var currentMonth by rememberSaveable { mutableIntStateOf(utcMonth(today)) }
    var selectedDay by rememberSaveable { mutableIntStateOf(utcDay(today)) }

    val daysInMonth = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply { clear(); set(currentYear, currentMonth - 1, 1) }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val shownDay = selectedDay.coerceAtMost(daysInMonth)
    val selectedUtc = utcMillisOf(currentYear, currentMonth, shownDay)

    val markedDays = remember(futureReminders, currentYear, currentMonth, daysInMonth) {
        (1..daysInMonth).filter { day ->
            val utc = utcMillisOf(currentYear, currentMonth, day)
            futureReminders.any { it.occursOn(utc) }
        }.toSet()
    }
    val tasks = remember(futureReminders, selectedUtc) {
        futureReminders
            .filter { it.occursOn(selectedUtc) }
            .sortedBy { it.hour * 60 + it.minute }
    }

    val sections = remember { List(3 + MAX_ANIMATED_TASKS) { Animatable(0f) } }
    LaunchedEffect(sections) {
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        sections.forEachIndexed { i, a ->
            launch { delay(CONTENT_STAGGER_MILLIS * i); a.animateTo(1f, spec) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .gardenBackdrop()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            PlantScreenHeader(
                title = stringResource(R.string.routine_title),
                onOpenSettings = onOpenSettings,
                modifier = Modifier.revealed(sections[0])
            )

            Spacer(Modifier.height(20.dp))

            RoutineCalendarCard(
                year = currentYear,
                month = currentMonth,
                selectedDay = shownDay,
                markedDays = markedDays,
                onYearChange = { currentYear = it },
                onMonthChange = { currentMonth = it },
                onDaySelected = { selectedDay = it },
                modifier = Modifier.revealed(sections[1])
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .revealed(sections[2]),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedUtc == today) {
                        stringResource(R.string.routine_today_care)
                    } else {
                        stringResource(R.string.routine_day_care, formatDayMonth(selectedUtc))
                    },
                    color = BotanikaGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                NewReminderButton(onClick = onNewReminder)
            }

            Spacer(Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.routine_empty),
                    color = BotanikaMuted,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }

            tasks.forEachIndexed { i, reminder ->
                CareTaskCard(
                    task = reminder.toCareTask(selectedUtc),
                    onDelete = { onDeleteReminder(reminder) },
                    onClick = { onEditReminder(reminder) },
                    modifier = Modifier.revealed(sections[3 + i.coerceAtMost(MAX_ANIMATED_TASKS - 1)])
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))
        }

        BottomNavBar(selected = MainTab.Routine, onSelected = onTabSelected)
    }
}

@Composable
private fun NewReminderButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BotanikaGreen)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DoodleIcon(DoodleIconType.Plus, Modifier.size(18.dp), color = Color.White)
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.routine_new_reminder),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun RoutineCalendarCard(
    year: Int,
    month: Int,
    selectedDay: Int,
    markedDays: Set<Int>,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, 10.dp)
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        CalendarHeader(
            year = year,
            month = month,
            onPreviousMonth = {
                if (month == 1) {
                    onMonthChange(12)
                    onYearChange(year - 1)
                } else {
                    onMonthChange(month - 1)
                }
            },
            onNextMonth = {
                if (month == 12) {
                    onMonthChange(1)
                    onYearChange(year + 1)
                } else {
                    onMonthChange(month + 1)
                }
            },
            onMonthChange = onMonthChange,
            onYearChange = onYearChange
        )

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth()) {
            Weekdays.forEach { dayName ->
                Text(
                    text = dayName,
                    color = BotanikaMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        CalendarDaysGrid(
            year = year,
            month = month,
            selectedDay = selectedDay,
            markedDays = markedDays,
            onDaySelected = onDaySelected
        )
    }
}

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    var monthMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BotanikaSage.copy(alpha = 0.6f))
                .clickable(role = Role.Button, onClick = onPreviousMonth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                color = BotanikaGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BotanikaBackground)
                        .clickable(role = Role.Button) { monthMenuOpen = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = MonthNames.getOrElse(month - 1) { "Nov" },
                        color = BotanikaGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "⌵", color = BotanikaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = monthMenuOpen,
                    onDismissRequest = { monthMenuOpen = false }
                ) {
                    MonthNames.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            text = { Text(name, color = BotanikaGreen) },
                            onClick = {
                                onMonthChange(index + 1)
                                monthMenuOpen = false
                            }
                        )
                    }
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BotanikaBackground)
                        .clickable(role = Role.Button) { yearMenuOpen = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = year.toString(),
                        color = BotanikaGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "⌵", color = BotanikaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = yearMenuOpen,
                    onDismissRequest = { yearMenuOpen = false }
                ) {
                    val years = (2024..2030).toList()
                    years.forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y.toString(), color = BotanikaGreen) },
                            onClick = {
                                onYearChange(y)
                                yearMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BotanikaSage.copy(alpha = 0.6f))
                .clickable(role = Role.Button, onClick = onNextMonth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "›",
                color = BotanikaGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    markedDays: Set<Int>,
    onDaySelected: (Int) -> Unit
) {
    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = firstDayOfWeek - 1

    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var cellIndex = 0
        while (cellIndex < totalCells) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val currentCell = cellIndex
                    val dayNumber = currentCell - startOffset + 1
                    val isCurrentMonth = dayNumber in 1..daysInMonth
                    val isNextMonth = dayNumber > daysInMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrentMonth) {
                            val isSelected = selectedDay == dayNumber
                            val isPinkBadge = !isSelected && markedDays.contains(dayNumber)

                            val backgroundModifier = when {
                                isSelected -> Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BotanikaGreen)
                                isPinkBadge -> Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SelectionPink)
                                else -> Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .then(backgroundModifier)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(role = Role.Button) { onDaySelected(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = if (isSelected) Color.White else BotanikaGreen,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isPinkBadge) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        } else if (isNextMonth) {
                            val nextMonthDay = dayNumber - daysInMonth
                            Text(
                                text = nextMonthDay.toString(),
                                color = NextMonthGray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    cellIndex++
                }
            }
        }
    }
}
