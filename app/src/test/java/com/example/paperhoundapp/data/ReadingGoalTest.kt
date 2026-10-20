package com.example.paperhoundapp.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class ReadingGoalTest {

    private lateinit var defaultLocale: Locale

    // Wednesday
    private val today = LocalDate.of(2026, 10, 21)

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun yearCoversTheCalendarYear() {
        assertEquals(LocalDate.of(2026, 1, 1), GoalPeriod.Year.start(today))
        assertEquals(LocalDate.of(2026, 12, 31), GoalPeriod.Year.end(today))
    }

    @Test
    fun monthCoversTheCalendarMonth() {
        assertEquals(LocalDate.of(2026, 10, 1), GoalPeriod.Month.start(today))
        assertEquals(LocalDate.of(2026, 10, 31), GoalPeriod.Month.end(today))
        assertEquals(LocalDate.of(2028, 2, 29), GoalPeriod.Month.end(LocalDate.of(2028, 2, 10)))
    }

    @Test
    fun weekStartsOnTheLocaleFirstDay() {
        assertEquals(LocalDate.of(2026, 10, 18), GoalPeriod.Week.start(today))
        assertEquals(LocalDate.of(2026, 10, 24), GoalPeriod.Week.end(today))

        Locale.setDefault(Locale.GERMANY)
        assertEquals(LocalDate.of(2026, 10, 19), GoalPeriod.Week.start(today))
        assertEquals(LocalDate.of(2026, 10, 25), GoalPeriod.Week.end(today))
    }

    @Test
    fun weekStartingTodayStartsToday() {
        val sunday = LocalDate.of(2026, 10, 18)
        assertEquals(sunday, GoalPeriod.Week.start(sunday))
    }

    @Test
    fun alreadyReadCountsOnlyInItsPeriod() {
        val goal = ReadingGoal(alreadyRead = 5, alreadyReadPeriodStart = LocalDate.of(2026, 1, 1))
        assertEquals(5, goal.alreadyReadIn(today))
        assertEquals(0, goal.alreadyReadIn(LocalDate.of(2027, 1, 1)))
        assertEquals(0, ReadingGoal(alreadyRead = 5).alreadyReadIn(today))
    }

    @Test
    fun alreadyReadStopsCountingWhenThePeriodChanges() {
        val goal = ReadingGoal(period = GoalPeriod.Month, alreadyRead = 3, alreadyReadPeriodStart = LocalDate.of(2026, 1, 1))
        assertEquals(0, goal.alreadyReadIn(today))
    }

    @Test
    fun finishedCountsBooksInThePeriodPlusAlreadyRead() {
        val books = listOf(
            Book("1", "In period", finishedAt = LocalDate.of(2026, 3, 1)),
            Book("2", "First day", finishedAt = LocalDate.of(2026, 1, 1)),
            Book("3", "Last day", finishedAt = LocalDate.of(2026, 12, 31)),
            Book("4", "Last year", finishedAt = LocalDate.of(2025, 12, 31)),
            Book("5", "Next year", finishedAt = LocalDate.of(2027, 1, 1)),
            Book("6", "Still reading", status = ReadingStatus.Reading),
        )
        val goal = ReadingGoal(alreadyRead = 2, alreadyReadPeriodStart = LocalDate.of(2026, 1, 1))
        assertEquals(5, goal.finished(books, today))
        assertEquals(0, ReadingGoal().finished(emptyList(), today))
    }

    @Test
    fun expectedFollowsTheElapsedShareOfThePeriod() {
        val goal = ReadingGoal(target = 12)
        assertEquals(0, goal.expected(LocalDate.of(2026, 1, 1)))
        assertEquals(6, goal.expected(LocalDate.of(2026, 7, 2)))
        assertEquals(12, goal.expected(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun expectedForAWeek() {
        val goal = ReadingGoal(target = 7, period = GoalPeriod.Week)
        // Wednesday is the 4th day of a Sunday-first week.
        assertEquals(4, goal.expected(today))
    }

    @Test
    fun defaultGoalIsTwelveBooksAYear() {
        val goal = ReadingGoal()
        assertEquals(12, goal.target)
        assertEquals(GoalPeriod.Year, goal.period)
        assertEquals(0, goal.alreadyRead)
        assertEquals(null, goal.alreadyReadPeriodStart)
    }
}
