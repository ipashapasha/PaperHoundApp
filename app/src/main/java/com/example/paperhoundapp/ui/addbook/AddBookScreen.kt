package com.example.paperhoundapp.ui.addbook

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.example.paperhoundapp.R
import com.example.paperhoundapp.data.Book
import com.example.paperhoundapp.data.BookFormat
import com.example.paperhoundapp.data.BookRepository
import com.example.paperhoundapp.data.Rating
import com.example.paperhoundapp.data.ReadingStatus
import com.example.paperhoundapp.ui.theme.PaperHoundAppTheme
import com.example.paperhoundapp.ui.theme.paperBackground
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

private val isbnAllowedChars = Regex("[0-9Xx-]")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddBookScreen(onSubmit: (Book) -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var coverUri by rememberSaveable { mutableStateOf<String?>(null) }
    var format by rememberSaveable { mutableStateOf(BookFormat.Paper) }
    var title by rememberSaveable { mutableStateOf("") }
    var isbn by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var readingNow by rememberSaveable { mutableStateOf(false) }
    var finishedOnEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var startedOnEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var rating by rememberSaveable { mutableStateOf<Rating?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            // Keep read access after the app restarts.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            coverUri = uri.toString()
        }
    }

    val canSubmit = title.isNotBlank()
    val submit = {
        onSubmit(
            Book(
                id = BookRepository.newId(),
                title = title.trim(),
                format = format,
                status = if (readingNow) ReadingStatus.Reading else ReadingStatus.Finished,
                startedAt = startedOnEpochDay?.takeIf { readingNow }?.let(LocalDate::ofEpochDay),
                finishedAt = LocalDate.ofEpochDay(finishedOnEpochDay).takeIf { !readingNow },
                isbn = isbn.trim().ifEmpty { null },
                link = link.trim().ifEmpty { null },
                coverUri = coverUri,
                rating = rating.takeIf { !readingNow },
            ),
        )
        onClose()
    }

    Scaffold(
        modifier = modifier.paperBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(stringResource(R.string.new_book)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close))
                    }
                },
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                val height = ButtonDefaults.MediumContainerHeight
                Button(
                    onClick = submit,
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = height),
                    contentPadding = ButtonDefaults.contentPaddingFor(height),
                ) {
                    Text(stringResource(R.string.add_to_collection), style = ButtonDefaults.textStyleFor(height))
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoverPicker(
                coverUri = coverUri,
                onPick = {
                    pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            FormatSelector(format, onFormatChange = { format = it })

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                supportingText = { Text(stringResource(R.string.field_required)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = isbn,
                onValueChange = { input -> isbn = input.filter { isbnAllowedChars.matches(it.toString()) }.uppercase() },
                label = { Text(stringResource(R.string.field_isbn)) },
                supportingText = { Text(stringResource(R.string.field_optional)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text(stringResource(R.string.field_link)) },
                supportingText = { Text(stringResource(R.string.field_optional)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            ReadingNowSwitch(readingNow, onReadingNowChange = { readingNow = it })

            if (readingNow) {
                DateField(
                    date = startedOnEpochDay?.let(LocalDate::ofEpochDay),
                    labelRes = R.string.field_started_on,
                    onClick = { showDatePicker = true },
                    onClear = { startedOnEpochDay = null },
                )
            } else {
                DateField(
                    date = LocalDate.ofEpochDay(finishedOnEpochDay),
                    labelRes = R.string.field_finished_on,
                    onClick = { showDatePicker = true },
                )

                RatingSelector(rating, onRatingChange = { rating = it })
            }
        }
    }

    if (showDatePicker) {
        BookDatePicker(
            initial = LocalDate.ofEpochDay(if (readingNow) startedOnEpochDay ?: LocalDate.now().toEpochDay() else finishedOnEpochDay),
            onConfirm = {
                if (readingNow) startedOnEpochDay = it.toEpochDay() else finishedOnEpochDay = it.toEpochDay()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun CoverPicker(coverUri: String?, onPick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier
            .width(160.dp)
            .aspectRatio(2f / 3f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUri != null) {
            AsyncImage(
                model = coverUri.toUri(),
                contentDescription = stringResource(R.string.change_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = stringResource(R.string.add_cover),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FormatSelector(format: BookFormat, onFormatChange: (BookFormat) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        ToggleButton(
            checked = format == BookFormat.Paper,
            onCheckedChange = { onFormatChange(BookFormat.Paper) },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.RadioButton },
        ) {
            Text(stringResource(R.string.format_paper))
        }
        ToggleButton(
            checked = format == BookFormat.Audio,
            onCheckedChange = { onFormatChange(BookFormat.Audio) },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.RadioButton },
        ) {
            Text(stringResource(R.string.format_audio))
        }
    }
}

@Composable
private fun ReadingNowSwitch(readingNow: Boolean, onReadingNowChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = readingNow, role = Role.Switch, onValueChange = onReadingNowChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.reading_now_toggle),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.reading_now_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        // The row handles toggling, so the switch itself is decorative.
        Switch(checked = readingNow, onCheckedChange = null)
    }
}

/** A null [date] renders an empty optional field; [onClear] adds a button to reset it. */
@Composable
private fun DateField(
    date: LocalDate?,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    // A read-only text field consumes taps, so open the picker on press release instead.
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) onClick()
        }
    }
    OutlinedTextField(
        value = date?.format(dateFormatter).orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(labelRes)) },
        supportingText = if (onClear != null) {
            { Text(stringResource(R.string.field_optional)) }
        } else {
            null
        },
        trailingIcon = {
            if (date != null && onClear != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.clear_date))
                }
            } else {
                IconButton(onClick = onClick) {
                    Icon(Icons.Rounded.DateRange, contentDescription = stringResource(R.string.pick_date))
                }
            }
        },
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDatePicker(initial: LocalDate, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let {
                        onConfirm(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        DatePicker(state)
    }
}

@Composable
private fun RatingSelector(rating: Rating?, onRatingChange: (Rating?) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.rating),
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

@Preview(showBackground = true)
@Composable
private fun AddBookScreenPreview() {
    PaperHoundAppTheme(dynamicColor = false) {
        AddBookScreen(onSubmit = {}, onClose = {})
    }
}
