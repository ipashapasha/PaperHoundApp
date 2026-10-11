package com.example.paperhoundapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.example.paperhoundapp.data.Book

private data class CoverPalette(val top: Color, val bottom: Color, val content: Color)

// Placeholder covers for books without a picked cover image.
private val coverPalettes = listOf(
    CoverPalette(Color(0xFF3E5C76), Color(0xFF1D2D44), Color(0xFFF0EBD8)),
    CoverPalette(Color(0xFFB5838D), Color(0xFF6D597A), Color(0xFFFFF4EC)),
    CoverPalette(Color(0xFF606C38), Color(0xFF283618), Color(0xFFFEFAE0)),
    CoverPalette(Color(0xFFDDA15E), Color(0xFFBC6C25), Color(0xFF2B1B0E)),
    CoverPalette(Color(0xFF8E9AAF), Color(0xFF4A4E69), Color(0xFFF2E9E4)),
    CoverPalette(Color(0xFFE5989B), Color(0xFFB56576), Color(0xFF2D1E2F)),
)

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val coverModifier = modifier
        .aspectRatio(2f / 3f)
        .clip(MaterialTheme.shapes.large)

    if (book.coverUri != null) {
        AsyncImage(
            model = book.coverUri.toUri(),
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = coverModifier,
        )
        return
    }

    val palette = coverPalettes[book.id.hashCode().mod(coverPalettes.size)]
    Box(coverModifier.background(Brush.verticalGradient(listOf(palette.top, palette.bottom)))) {
        // Spine shadow
        Box(
            Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(Color.Black.copy(alpha = 0.15f)),
        )
        Text(
            text = book.title,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif),
            color = palette.content,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, end = 8.dp, bottom = 10.dp),
        )
    }
}

@Composable
fun BookCard(book: Book, modifier: Modifier = Modifier, showRating: Boolean = false) {
    val ratingLabel = book.rating?.let { stringResource(it.labelRes) }.orEmpty()
    Column(modifier) {
        BookCover(book, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (book.author != null) {
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showRating && book.rating != null) {
            Text(
                text = book.rating.emoji,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics { contentDescription = ratingLabel },
            )
        }
    }
}
