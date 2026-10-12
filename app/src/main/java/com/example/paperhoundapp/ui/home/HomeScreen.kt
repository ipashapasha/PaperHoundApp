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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
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
import com.example.paperhoundapp.data.BookRepository
import com.example.paperhoundapp.data.SampleBooks
import com.example.paperhoundapp.data.Shelf
import com.example.paperhoundapp.data.finishedInYear
import com.example.paperhoundapp.data.shelf
import com.example.paperhoundapp.ui.components.BookCard
import com.example.paperhoundapp.ui.theme.PaperHoundAppTheme
import com.example.paperhoundapp.ui.theme.paperBackground
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

private const val PREVIEW_BOOK_COUNT = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    books: List<Book>,
    onOpenShelf: (Shelf) -> Unit,
    onAddBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val today = remember { LocalDate.now() }
    val sectionModifier = Modifier.padding(horizontal = 16.dp)

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
            verticalArrangement = Arrangement.spacedBy(32.dp),
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
            item {
                YearProgressCard(
                    year = today.year,
                    dayOfYear = today.dayOfYear,
                    daysInYear = today.lengthOfYear(),
                    finished = books.finishedInYear(today.year),
                    goal = BookRepository.YEARLY_GOAL,
                    modifier = sectionModifier,
                )
            }
            item {
                ShelfSection(Shelf.PaperLibrary, books, onSeeAll = { onOpenShelf(Shelf.PaperLibrary) }, modifier = sectionModifier)
            }
            item {
                ShelfSection(Shelf.AudioLibrary, books, onSeeAll = { onOpenShelf(Shelf.AudioLibrary) }, modifier = sectionModifier)
            }
        }
    }
}

@Composable
private fun ShelfSection(shelf: Shelf, allBooks: List<Book>, onSeeAll: () -> Unit, modifier: Modifier = Modifier) {
    val books = allBooks.shelf(shelf)
    Column(modifier) {
        SectionHeader(
            title = stringResource(shelf.titleRes),
            onSeeAll = onSeeAll.takeIf { books.size > PREVIEW_BOOK_COUNT },
        )
        Spacer(Modifier.height(12.dp))
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
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLargeEmphasized,
            modifier = Modifier.weight(1f),
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(stringResource(R.string.see_all))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun YearProgressCard(
    year: Int,
    dayOfYear: Int,
    daysInYear: Int,
    finished: Int,
    goal: Int,
    modifier: Modifier = Modifier,
) {
    val expected = (goal * dayOfYear.toFloat() / daysInYear).roundToInt()
    val diff = finished - expected
    val pace = when {
        diff > 0 -> pluralStringResource(R.plurals.year_goal_ahead, diff, diff)
        diff < 0 -> pluralStringResource(R.plurals.year_goal_behind, abs(diff), abs(diff))
        else -> stringResource(R.string.year_goal_on_track)
    }
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.year_goal_title, year),
                style = MaterialTheme.typography.labelLarge,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = finished.toString(),
                    style = MaterialTheme.typography.displayLargeEmphasized,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = pluralStringResource(R.plurals.year_goal_of_books, goal, goal),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearWavyProgressIndicator(
                progress = { (finished.toFloat() / goal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(12.dp))
            Text(text = pace, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PaperHoundAppTheme(dynamicColor = false) {
        HomeScreen(SampleBooks.all, onOpenShelf = {}, onAddBook = {})
    }
}
