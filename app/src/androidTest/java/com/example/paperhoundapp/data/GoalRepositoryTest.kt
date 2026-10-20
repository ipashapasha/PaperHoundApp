package com.example.paperhoundapp.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class GoalRepositoryTest {

    private val repository = GoalRepository(ApplicationProvider.getApplicationContext())

    // update() doesn't wait for the write, and a collector started before it may see the new value late,
    // so read the stored goal again until the write shows up.
    private fun updateAndAwait(goal: ReadingGoal) = runBlocking {
        repository.update(goal)
        withTimeoutOrNull(5_000) {
            while (repository.goal.first() != goal) delay(20)
            goal
        } ?: throw AssertionError("Expected $goal to be stored, but it is ${repository.goal.first()}")
    }

    @Before
    @After
    fun resetGoal() {
        updateAndAwait(ReadingGoal())
    }

    @Test
    fun storesTheWholeGoal() {
        val goal = ReadingGoal(
            target = 4,
            period = GoalPeriod.Month,
            alreadyRead = 2,
            alreadyReadPeriodStart = LocalDate.of(2026, 10, 1),
        )
        assertEquals(goal, updateAndAwait(goal))
    }

    @Test
    fun clearsThePeriodStartWhenNothingWasReadBefore() {
        updateAndAwait(ReadingGoal(target = 5, alreadyRead = 1, alreadyReadPeriodStart = LocalDate.of(2026, 1, 1)))

        val goal = ReadingGoal(target = 5, period = GoalPeriod.Week)
        assertEquals(goal, updateAndAwait(goal))
    }

    @Test
    fun aNewRepositorySeesTheStoredGoal() = runBlocking {
        val goal = ReadingGoal(target = 30)
        updateAndAwait(goal)
        assertEquals(goal, GoalRepository(ApplicationProvider.getApplicationContext()).goal.first())
    }
}
