package com.example.paperhoundapp.ui.addbook

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.example.paperhoundapp.R
import com.example.paperhoundapp.data.Book
import com.example.paperhoundapp.data.BookDescription
import com.example.paperhoundapp.data.BookFormat
import com.example.paperhoundapp.data.BookRepository
import com.example.paperhoundapp.data.ReadingStatus
import com.example.paperhoundapp.ui.components.ArchiveIcon
import com.example.paperhoundapp.ui.components.UnderlinedTextField
import com.example.paperhoundapp.ui.theme.BeigeButtonDark
import com.example.paperhoundapp.ui.theme.BeigeButtonLight
import com.example.paperhoundapp.ui.theme.OnBeigeButtonDark
import com.example.paperhoundapp.ui.theme.OnBeigeButtonLight
import com.example.paperhoundapp.ui.theme.PaperHoundAppTheme
import com.example.paperhoundapp.ui.theme.paperBackground
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Keeps the searching overlay up long enough to read, even when the answer is instant. */
private const val MIN_SEARCH_MILLIS = 900L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddBookScreen(
    onSubmit: (Book) -> Unit,
    findDescriptions: suspend (title: String) -> List<BookDescription>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // A cover picked by the user wins over the one found with the description.
    var coverUri by rememberSaveable { mutableStateOf<String?>(null) }
    var foundCoverUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var format by rememberSaveable { mutableStateOf(BookFormat.Paper) }
    var title by rememberSaveable { mutableStateOf("") }
    var showArchiveSheet by rememberSaveable { mutableStateOf(false) }

    // The title the shown description belongs to; null until the first search finishes.
    var describedTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionText by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionAuthor by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionFailed by rememberSaveable { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    // Books with the searched title by different authors, waiting for the user to pick one.
    var candidates by remember { mutableStateOf(emptyList<BookDescription>()) }
    var candidatesTitle by remember { mutableStateOf("") }
    var titleFocused by remember { mutableStateOf(false) }

    fun showDescription(forTitle: String, description: BookDescription?) {
        descriptionText = description?.text
        descriptionUrl = description?.sourceUrl
        descriptionAuthor = description?.author
        foundCoverUrl = description?.coverUrl
        describedTitle = forTitle
        candidates = emptyList()
    }

    val searchDescription = search@{
        val query = title.trim()
        // A failed search is retried on the next focus change even if the title is the same.
        if (searching || candidates.isNotEmpty() || (query == describedTitle && !descriptionFailed)) return@search
        if (query.isEmpty()) {
            describedTitle = null
            return@search
        }
        scope.launch {
            searching = true
            try {
                val startedAt = System.currentTimeMillis()
                val results = try {
                    findDescriptions(query).also { descriptionFailed = false }
                } catch (e: IOException) {
                    descriptionFailed = true
                    emptyList()
                }
                delay(MIN_SEARCH_MILLIS - (System.currentTimeMillis() - startedAt))
                if (results.size > 1) {
                    candidatesTitle = query
                    candidates = results
                } else {
                    showDescription(query, results.firstOrNull())
                }
            } finally {
                searching = false
            }
        }
    }

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
    fun newBook(): Book {
        // The found author and cover only count if they still belong to the current title.
        val described = describedTitle == title.trim()
        return Book(
            id = BookRepository.newId(),
            title = title.trim(),
            author = descriptionAuthor.takeIf { described },
            format = format,
            coverUri = coverUri ?: foundCoverUrl.takeIf { described },
        )
    }

    val readNow = {
        onSubmit(newBook().copy(status = ReadingStatus.Reading, startedAt = LocalDate.now()))
        onClose()
    }

    val overlayShown = searching || candidates.isNotEmpty()
    val blurRadius by animateDpAsState(
        targetValue = if (overlayShown) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 450),
    )

    Box(modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .paperBackground()
                .blur(blurRadius),
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
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val height = ButtonDefaults.MediumContainerHeight
                    Button(
                        onClick = readNow,
                        enabled = canSubmit,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = height),
                        contentPadding = ButtonDefaults.contentPaddingFor(height),
                    ) {
                        Text(stringResource(R.string.read_now), style = ButtonDefaults.textStyleFor(height))
                    }
                    // A book read earlier goes straight to the library after picking the month and reaction.
                    FilledTonalIconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showArchiveSheet = true
                        },
                        enabled = canSubmit,
                        colors = beigeIconButtonColors(),
                        modifier = Modifier.size(height),
                    ) {
                        Icon(ArchiveIcon, contentDescription = stringResource(R.string.add_read_earlier))
                    }
                }
            },
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    // Tapping outside the fields drops focus, which starts the description search.
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CoverPicker(
                    coverUri = coverUri ?: foundCoverUrl,
                    framed = coverUri == null,
                    onPick = {
                        pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                FormatSelector(format, onFormatChange = { format = it })

                UnderlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.field_title)) },
                    isError = title.isBlank(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (titleFocused && !it.isFocused) searchDescription()
                            titleFocused = it.isFocused
                        },
                )

                AnimatedVisibility(
                    visible = describedTitle != null,
                    enter = fadeIn(tween(400, delayMillis = 200)) + expandVertically(tween(400, delayMillis = 200)),
                    exit = fadeOut(tween(200)),
                ) {
                    DescriptionBlock(
                        text = descriptionText,
                        author = descriptionAuthor,
                        sourceUrl = descriptionUrl,
                        failed = descriptionFailed,
                    )
                }
            }
        }

        AnimatedVisibility(visible = overlayShown, enter = fadeIn(tween(300)), exit = fadeOut(tween(450))) {
            DescriptionOverlay(
                searching = searching,
                candidates = candidates,
                title = candidatesTitle,
                onChoose = { showDescription(candidatesTitle, it) },
            )
        }
    }

    BackHandler(enabled = candidates.isNotEmpty()) { showDescription(candidatesTitle, null) }

    if (showArchiveSheet) {
        ArchiveBookSheet(
            onSave = { finishedAt, rating ->
                showArchiveSheet = false
                onSubmit(newBook().copy(status = ReadingStatus.Finished, finishedAt = finishedAt, rating = rating))
                onClose()
            },
            onDismiss = { showArchiveSheet = false },
        )
    }
}

@Composable
private fun beigeIconButtonColors(): IconButtonColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = if (dark) BeigeButtonDark else BeigeButtonLight,
        contentColor = if (dark) OnBeigeButtonDark else OnBeigeButtonLight,
    )
}

/**
 * Covers the blurred screen and swallows touches: a loading state while searching, then author cards
 * when several books share the title. [onChoose] gets null when none of them fits.
 */
@Composable
private fun DescriptionOverlay(
    searching: Boolean,
    candidates: List<BookDescription>,
    title: String,
    onChoose: (BookDescription?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the cards on screen while the overlay fades out after a choice clears the list.
    var lastCandidates by remember { mutableStateOf(candidates) }
    LaunchedEffect(candidates) {
        if (candidates.isNotEmpty()) lastCandidates = candidates
    }
    val shownCandidates = if (searching) emptyList() else candidates.ifEmpty { lastCandidates }
    Box(
        modifier
            .fillMaxSize()
            // Blur needs Android 12, so the scrim alone keeps older devices readable.
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .pointerInput(Unit) { detectTapGestures { } }
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = shownCandidates,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "description overlay",
        ) { shown ->
            if (shown.isEmpty()) SearchingIndicator() else AuthorChoice(shown, title, onChoose)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchingIndicator(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    )
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        LoadingIndicator(Modifier.size(72.dp))
        Spacer(Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.searching_description),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(pulse),
        )
    }
}

@Composable
private fun AuthorChoice(
    candidates: List<BookDescription>,
    title: String,
    onChoose: (BookDescription?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.choose_author_title, title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.choose_author_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        candidates.forEachIndexed { index, candidate ->
            AuthorCard(candidate, appearDelayMillis = 120L * index, onClick = { onChoose(candidate) })
        }
        TextButton(onClick = { onChoose(null) }) {
            Text(stringResource(R.string.choose_author_none))
        }
    }
}

/** Pops in after [appearDelayMillis] so the cards arrive one after another. */
@Composable
private fun AuthorCard(
    candidate: BookDescription,
    appearDelayMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(appearDelayMillis)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) +
            scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), initialScale = 0.8f) +
            slideInVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) { it / 3 },
        modifier = modifier,
    ) {
        ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (candidate.coverUrl != null) {
                    AsyncImage(
                        model = candidate.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(48.dp)
                            .aspectRatio(2f / 3f)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = candidate.author ?: stringResource(R.string.unknown_author),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = candidate.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** A read-only description; a null [text] means nothing was found, or the search [failed]. */
@Composable
private fun DescriptionBlock(
    text: String?,
    author: String?,
    sourceUrl: String?,
    failed: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (text != null && author != null) {
                    stringResource(R.string.description_by, author)
                } else {
                    stringResource(R.string.description)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (text == null) {
                Text(
                    text = stringResource(if (failed) R.string.description_failed else R.string.description_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            if (sourceUrl != null) {
                val linkStyles = TextLinkStyles(
                    SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
                )
                val label = stringResource(R.string.description_source)
                Text(
                    text = buildAnnotatedString {
                        withLink(LinkAnnotation.Url(sourceUrl, linkStyles)) { append(label) }
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** A [framed] cover is fitted inside a mat instead of filling the box, since online covers vary in shape. */
@Composable
private fun CoverPicker(coverUri: String?, framed: Boolean, onPick: () -> Unit, modifier: Modifier = Modifier) {
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
        AnimatedContent(
            targetState = coverUri,
            transitionSpec = {
                (fadeIn(tween(350)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.85f))
                    .togetherWith(fadeOut(tween(200)))
            },
            contentAlignment = Alignment.Center,
            label = "cover",
        ) { uri ->
            if (uri != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(if (framed) 10.dp else 0.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = uri.toUri(),
                        contentDescription = stringResource(R.string.change_cover),
                        contentScale = if (framed) ContentScale.Fit else ContentScale.Crop,
                        modifier = if (framed) Modifier.clip(MaterialTheme.shapes.small) else Modifier.matchParentSize(),
                    )
                }
            } else {
                AddCoverHint()
            }
        }
    }
}

@Composable
private fun AddCoverHint(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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

@Preview(showBackground = true)
@Composable
private fun AddBookScreenPreview() {
    PaperHoundAppTheme(dynamicColor = false) {
        AddBookScreen(onSubmit = {}, findDescriptions = { emptyList() }, onClose = {})
    }
}
