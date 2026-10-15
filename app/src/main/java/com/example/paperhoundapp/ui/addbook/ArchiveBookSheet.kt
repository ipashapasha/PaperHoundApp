package com.example.paperhoundapp.ui.addbook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paperhoundapp.R
import com.example.paperhoundapp.data.Rating
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val MONTHS_PER_ROW = 4

/** Asks for the month a book was read in and the reaction to it; [onSave] gets the finish date. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArchiveBookSheet(
    onSave: (finishedAt: LocalDate, rating: Rating?) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    var year by rememberSaveable { mutableIntStateOf(today.year) }
    var month by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var rating by rememberSaveable { mutableStateOf<Rating?>(null) }

    val save = {
        val readIn = YearMonth.of(year, month)
        // Only the month is known; the current month counts as today so weekly goals still include it.
        onSave(if (readIn == YearMonth.from(today)) today else readIn.atEndOfMonth(), rating)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.read_earlier_title), style = MaterialTheme.typography.headlineSmall)

            MonthPicker(
                year = year,
                month = month,
                latest = YearMonth.from(today),
                onYearChange = { newYear ->
                    year = newYear
                    // Moving to the current year can leave a future month selected.
                    if (newYear == today.year && month > today.monthValue) month = today.monthValue
                },
                onMonthChange = { month = it },
            )

            RatingSelector(rating, onRatingChange = { rating = it })

            val height = ButtonDefaults.MediumContainerHeight
            Button(
                onClick = save,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Text(stringResource(R.string.add_to_library), style = ButtonDefaults.textStyleFor(height))
            }
        }
    }
}

/** A year switcher over a grid of months; months after [latest] can't be picked. */
@Composable
private fun MonthPicker(
    year: Int,
    month: Int,
    latest: YearMonth,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.read_in_month),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onYearChange(year - 1) }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_year))
            }
            Text(year.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onYearChange(year + 1) }, enabled = year < latest.year) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.next_year))
            }
        }
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Month.entries.chunked(MONTHS_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        val selected = option.value == month
                        val enabled = !YearMonth.of(year, option).isAfter(latest)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer,
                                )
                                .selectable(
                                    selected = selected,
                                    enabled = enabled,
                                    role = Role.RadioButton,
                                    onClick = { onMonthChange(option.value) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = option.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.alpha(if (enabled) 1f else 0.38f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSelector(rating: Rating?, onRatingChange: (Rating?) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.reaction),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Rating.entries.forEach { option ->
                val selected = option == rating
                val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
                val scale by animateFloatAsState(if (selected) 1.25f else 1f, spatialSpec)
                val label = stringResource(option.labelRes)
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                        )
                        // Tapping the selected emoji again clears the rating.
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onRatingChange(if (selected) null else option) },
                        )
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.emoji,
                        fontSize = 26.sp,
                        modifier = Modifier
                            .scale(scale)
                            .alpha(if (rating == null || selected) 1f else 0.5f),
                    )
                }
            }
        }
    }
}
