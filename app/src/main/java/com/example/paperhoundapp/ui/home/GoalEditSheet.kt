package com.example.paperhoundapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paperhoundapp.R
import com.example.paperhoundapp.data.GoalPeriod
import com.example.paperhoundapp.data.ReadingGoal
import java.time.LocalDate

private const val MAX_DIGITS = 4

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GoalEditSheet(
    goal: ReadingGoal,
    today: LocalDate,
    onSave: (ReadingGoal) -> Unit,
    onDismiss: () -> Unit,
) {
    var period by rememberSaveable { mutableStateOf(goal.period) }
    var target by rememberSaveable { mutableStateOf(goal.target.toString()) }
    var alreadyRead by rememberSaveable { mutableStateOf(goal.alreadyReadIn(today).takeIf { it > 0 }?.toString().orEmpty()) }

    val targetValue = target.toIntOrNull()?.takeIf { it > 0 }
    val alreadyReadValue = alreadyRead.toIntOrNull() ?: 0
    val save = {
        if (targetValue != null) {
            onSave(
                ReadingGoal(
                    target = targetValue,
                    period = period,
                    alreadyRead = alreadyReadValue,
                    alreadyReadPeriodStart = period.start(today).takeIf { alreadyReadValue > 0 },
                ),
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.edit_goal), style = MaterialTheme.typography.headlineSmall)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.goal_period), style = MaterialTheme.typography.titleMedium)
                PeriodSelector(period, onPeriodChange = { period = it })
            }

            OutlinedTextField(
                value = target,
                onValueChange = { input -> target = input.filter(Char::isDigit).take(MAX_DIGITS) },
                label = { Text(stringResource(R.string.goal_target)) },
                supportingText = if (targetValue == null) {
                    { Text(stringResource(R.string.goal_target_error)) }
                } else {
                    null
                },
                isError = targetValue == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = alreadyRead,
                onValueChange = { input -> alreadyRead = input.filter(Char::isDigit).take(MAX_DIGITS) },
                label = { Text(stringResource(period.alreadyReadLabelRes)) },
                supportingText = { Text(stringResource(R.string.goal_already_read_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            val height = ButtonDefaults.MediumContainerHeight
            Button(
                onClick = save,
                enabled = targetValue != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Text(stringResource(R.string.save), style = ButtonDefaults.textStyleFor(height))
            }
        }
    }
}

private val GoalPeriod.alreadyReadLabelRes: Int
    get() = when (this) {
        GoalPeriod.Year -> R.string.goal_already_read_year
        GoalPeriod.Month -> R.string.goal_already_read_month
        GoalPeriod.Week -> R.string.goal_already_read_week
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PeriodSelector(period: GoalPeriod, onPeriodChange: (GoalPeriod) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        GoalPeriod.entries.forEachIndexed { index, option ->
            ToggleButton(
                checked = period == option,
                onCheckedChange = { onPeriodChange(option) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    GoalPeriod.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
            ) {
                Text(stringResource(option.labelRes))
            }
        }
    }
}
