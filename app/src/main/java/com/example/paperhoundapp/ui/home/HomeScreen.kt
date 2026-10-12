package com.example.paperhoundapp.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paperhoundapp.R
import com.example.paperhoundapp.data.Book
import com.example.paperhoundapp.data.GoalPeriod
import com.example.paperhoundapp.data.ReadingGoal
import com.example.paperhoundapp.data.SampleBooks
import com.example.paperhoundapp.data.Shelf
import com.example.paperhoundapp.data.shelf
import com.example.paperhoundapp.ui.components.BookCard
import com.example.paperhoundapp.ui.theme.PaperHoundAppTheme
import com.example.paperhoundapp.ui.theme.paperBackground
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private const val PREVIEW_BOOK_COUNT = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    books: List<Book>,
    goal: ReadingGoal,
    onGoalChange: (ReadingGoal) -> Unit,
    onOpenShelf: (Shelf) -> Unit,
    onAddBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val today = remember { LocalDate.now() }
    val sectionModifier = Modifier.padding(horizontal = 16.dp)
    var editingGoal by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.paperBackground(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            MediumExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.add_book)) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                onClick = onAddBook,
                expanded = fabExpanded,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                // Keep the last row clear of the FAB.
                bottom = innerPadding.calculateBottomPadding() + 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                Image(
                    painter = painterResource(R.drawable.header_greyhound),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ShelfSection(Shelf.Reading, books, onSeeAll = { onOpenShelf(Shelf.Reading) }, modifier = sectionModifier)
            }
            item { SectionDivider(sectionModifier) }
            item {
                ReadingGoalSection(
                    goal = goal,
                    finished = goal.finished(books, today),
                    today = today,
                    onEdit = { editingGoal = true },
                    modifier = sectionModifier,
                )
            }
            item { SectionDivider(sectionModifier) }
            item {
                ShelfSection(Shelf.PaperLibrary, books, onSeeAll = { onOpenShelf(Shelf.PaperLibrary) }, modifier = sectionModifier)
            }
            item { SectionDivider(sectionModifier) }
            item {
                ShelfSection(Shelf.AudioLibrary, books, onSeeAll = { onOpenShelf(Shelf.AudioLibrary) }, modifier = sectionModifier)
            }
        }
    }

    if (editingGoal) {
        GoalEditSheet(
            goal = goal,
            today = today,
            onSave = {
                onGoalChange(it)
                editingGoal = false
            },
            onDismiss = { editingGoal = false },
        )
    }
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ShelfSection(shelf: Shelf, allBooks: List<Book>, onSeeAll: () -> Unit, modifier: Modifier = Modifier) {
    val books = allBooks.shelf(shelf)
    Column(modifier) {
        SectionHeader(title = stringResource(shelf.titleRes)) {
            if (books.size > PREVIEW_BOOK_COUNT) {
                TextButton(onClick = onSeeAll) {
                    Text(stringResource(R.string.see_all))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (books.isEmpty()) {
            Text(
                text = stringResource(R.string.shelf_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            BookRow(books.take(PREVIEW_BOOK_COUNT))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMediumEmphasized,
            modifier = Modifier.weight(1f),
        )
        action()
    }
}

/** Always lays out [PREVIEW_BOOK_COUNT] equal columns so card widths stay stable. */
@Composable
private fun BookRow(books: List<Book>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(PREVIEW_BOOK_COUNT) { index ->
            val book = books.getOrNull(index)
            if (book != null) {
                BookCard(book, Modifier.weight(1f), showRating = true)
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun goalTitle(period: GoalPeriod, today: LocalDate): String = when (period) {
    GoalPeriod.Year -> stringResource(R.string.goal_title_year, today.year)
    GoalPeriod.Month -> stringResource(
        R.string.goal_title_month,
        today.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()),
    )
    GoalPeriod.Week -> stringResource(R.string.goal_title_week)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReadingGoalSection(
    goal: ReadingGoal,
    finished: Int,
    today: LocalDate,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diff = finished - goal.expected(today)
    val pace = when {
        diff > 0 -> pluralStringResource(R.plurals.year_goal_ahead, diff, diff)
        diff < 0 -> pluralStringResource(R.plurals.year_goal_behind, abs(diff), abs(diff))
        else -> stringResource(R.string.year_goal_on_track)
    }
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Column(modifier) {
        SectionHeader(title = goalTitle(goal.period, today)) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_goal))
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = finished.toString(),
                        style = MaterialTheme.typography.displayLargeEmphasized,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = pluralStringResource(R.plurals.year_goal_of_books, goal.target, goal.target),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                LinearWavyProgressIndicator(
                    progress = { (finished.toFloat() / goal.target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f),
                )
                Spacer(Modifier.height(12.dp))
                Text(text = pace, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PaperHoundAppTheme(dynamicColor = false) {
        HomeScreen(SampleBooks.all, ReadingGoal(), onGoalChange = {}, onOpenShelf = {}, onAddBook = {})
    }
}
