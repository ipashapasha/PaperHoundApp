package com.example.paperhoundapp.data

import androidx.annotation.StringRes
import com.example.paperhoundapp.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

enum class GoalPeriod(@StringRes val labelRes: Int) {
    Year(R.string.goal_period_year),
    Month(R.string.goal_period_month),
    Week(R.string.goal_period_week);

    fun start(today: LocalDate): LocalDate = when (this) {
        Year -> today.withDayOfYear(1)
        Month -> today.withDayOfMonth(1)
        Week -> today.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek))
    }

    fun end(today: LocalDate): LocalDate = when (this) {
        Year -> start(today).plusYears(1)
        Month -> start(today).plusMonths(1)
        Week -> start(today).plusWeeks(1)
    }.minusDays(1)
}

data class ReadingGoal(
    val target: Int = 12,
    val period: GoalPeriod = GoalPeriod.Year,
    /** Books finished before the user started tracking them in the app. */
    val alreadyRead: Int = 0,
    /** Start of the period [alreadyRead] was entered for; it stops counting once that period is over. */
    val alreadyReadPeriodStart: LocalDate? = null,
) {
    fun alreadyReadIn(today: LocalDate): Int =
        if (alreadyReadPeriodStart == period.start(today)) alreadyRead else 0

    fun finished(books: List<Book>, today: LocalDate): Int {
        val range = period.start(today)..period.end(today)
        return alreadyReadIn(today) + books.count { it.finishedAt != null && it.finishedAt in range }
    }

    /** How many books should be finished by [today] to stay on pace. */
    fun expected(today: LocalDate): Int {
        val start = period.start(today)
        val elapsed = ChronoUnit.DAYS.between(start, today) + 1
        val length = ChronoUnit.DAYS.between(start, period.end(today)) + 1
        return Math.round(target.toFloat() * elapsed / length)
    }
}
