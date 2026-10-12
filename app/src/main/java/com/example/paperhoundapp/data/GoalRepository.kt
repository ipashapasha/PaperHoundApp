package com.example.paperhoundapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

private val Context.goalDataStore by preferencesDataStore(name = "reading_goal")

/** Reading goal settings stored in local preferences. */
class GoalRepository(context: Context) {

    private val dataStore = context.goalDataStore

    // Writes outlive the sheet that started them.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val goal: Flow<ReadingGoal> = dataStore.data.map { prefs ->
        val default = ReadingGoal()
        ReadingGoal(
            target = prefs[TARGET] ?: default.target,
            period = prefs[PERIOD]?.let { name -> GoalPeriod.entries.find { it.name == name } } ?: default.period,
            alreadyRead = prefs[ALREADY_READ] ?: default.alreadyRead,
            alreadyReadPeriodStart = prefs[ALREADY_READ_PERIOD_START]?.let(LocalDate::ofEpochDay),
        )
    }

    fun update(goal: ReadingGoal) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[TARGET] = goal.target
                prefs[PERIOD] = goal.period.name
                prefs[ALREADY_READ] = goal.alreadyRead
                val periodStart = goal.alreadyReadPeriodStart
                if (periodStart != null) prefs[ALREADY_READ_PERIOD_START] = periodStart.toEpochDay()
                else prefs.remove(ALREADY_READ_PERIOD_START)
            }
        }
    }

    private companion object {
        val TARGET = intPreferencesKey("target")
        val PERIOD = stringPreferencesKey("period")
        val ALREADY_READ = intPreferencesKey("already_read")
        val ALREADY_READ_PERIOD_START = longPreferencesKey("already_read_period_start")
    }
}
