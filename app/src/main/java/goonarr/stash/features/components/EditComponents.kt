package goonarr.stash.features.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashDynamicColors
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

/**
 * A section in an edit screen with a title, optional count/dot/icon, and content.
 *
 * @param title The title of the section.
 * @param count Optional count to display next to the title.
 * @param dotColor Color of the separator dot. Defaults to onSurfaceVariant.
 * @param iconContent Optional icon content to display next to the title.
 * @param content The main content of the section.
 */
@Composable
fun EditSection(
    title: String,
    count: Int? = null,
    dotColor: Color? = null,
    showDivider: Boolean = true,
    iconContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (count != null || iconContent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelLarge,
                        color = dotColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    iconContent?.invoke()

                    if (count != null) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        content()
    }
}

/**
 * A standardized text field for edit screens.
 *
 * @param value The current text value.
 * @param onValueChange Callback when the text changes.
 * @param label The label text for the field.
 * @param modifier Optional [Modifier].
 * @param singleLine Whether the text field should be restricted to a single line.
 * @param minLines The minimum number of lines for the text field.
 */
@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val dynamicColors = StashTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(StashTokens.Radius.Card),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = dynamicColors.primary.copy(alpha = 0.2f),
            unfocusedContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
            focusedBorderColor = dynamicColors.primary,
            unfocusedBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
            focusedLabelColor = dynamicColors.primary,
            unfocusedLabelColor = dynamicColors.primary.copy(alpha = 0.5f),
            cursorColor = dynamicColors.primary
        )
    )
}

/**
 * A helper composable that renders a list of children with staggered entry animation.
 * eliminating the need for boilerplate state management in each screen.
 *
 * @param modifier Modifier for the Column.
 * @param children List of composable lambdas to be rendered.
 */
@Composable
fun StaggeredEditList(
    modifier: Modifier = Modifier,
    children: List<@Composable ColumnScope.() -> Unit>
) {
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        children.forEachIndexed { index, child ->
            StaggeredEntry(visible = visibleState.targetState, index = index) {
                child()
            }
        }
    }
}

/**
 * A shared scaffold for all edit screens to ensure consistency.
 *
 * @param title Screen title.
 * @param onBackClick Callback for back/cancel.
 * @param onSave Callback for save.
 * @param onRetry Callback for error retry, if null, errors won't show retry button.
 * @param uiState The standardized UI state of the screen.
 * @param dynamicColors Dynamic colors to apply to the screen.
 * @param content The main content of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedEditScaffold(
    title: String,
    onBackClick: () -> Unit,
    onSave: () -> Unit,
    onRetry: (() -> Unit)? = null,
    uiState: SharedEditUiState,
    dynamicColors: StashDynamicColors = StashTheme.colors,
    content: @Composable () -> Unit
) {
    val isLoading = uiState is SharedEditUiState.Loading
    val isSaving = uiState is SharedEditUiState.Saving
    val errorMessage = (uiState as? SharedEditUiState.Error)?.message

    Scaffold(
        containerColor = dynamicColors.primary.copy(alpha = 0.15f),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    if (!isLoading) {
                        Button(
                            onClick = onSave,
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = dynamicColors.primary
                            )
                        ) {
                            if (isSaving) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = dynamicColors.primary
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = dynamicColors.complementary,
                    navigationIconContentColor = dynamicColors.complementary,
                    actionIconContentColor = dynamicColors.primary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = dynamicColors.primary)
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                        if (onRetry != null) {
                            Button(onClick = onRetry) { Text("Retry") }
                        }
                    }
                }

                else -> content()
            }
        }
    }
}

/**
 * Shared background for edit screens.
 * Displays a blurred image if provided, or a solid color background.
 *
 * @param imageUrl URL of the image to blur.
 */
@Composable
fun SharedEditBackground(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
        }
    }
}

/**
 * A row with a label and a switch, optionally wrapped in an outlined card.
 *
 * @param label The label text.
 * @param checked Whether the switch is checked.
 * @param onCheckedChange Callback when the switch is toggled.
 * @param inCard Whether to wrap the row in an [OutlinedCard].
 */
@Composable
fun EditSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    inCard: Boolean = true
) {
    val content = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (inCard) 16.dp else 0.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }

    if (inCard) {
        OutlinedCard(content = { content() })
    } else {
        content()
    }
}

/**
 * Hero image section for edit screens.
 *
 * Displays a thumbnail image with an optional delete button.
 *
 * @param imageUrl URL of the image.
 * @param onDeleteImage Optional callback to clear the image.
 * @param contentDescription Description for accessibility.
 * @param aspectRatio Aspect ratio of the image container.
 * @param contentScale How to scale the image content.
 */
@Composable
fun ThumbnailSection(
    imageUrl: Any?,
    onDeleteImage: (() -> Unit)? = null,
    contentDescription: String = "Thumbnail",
    aspectRatio: Float = 16f / 9f,
    contentScale: ContentScale = ContentScale.Fit
) {
    var isError by remember(imageUrl) { mutableStateOf(false) }
    if (imageUrl == null || isError) return

    val haptic = LocalStashHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(shape = RoundedCornerShape(size = StashTokens.Radius.Card))
            .background(Color.Black.copy(alpha = StashTokens.Alpha.CardBackground))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(imageUrl)
                .crossfade(enable = true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            onError = {
                Timber.w("❌ Failed to load thumbnail image: ${it.result.throwable.message}")
                isError = true
            }
        )

        if (onDeleteImage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = StashTokens.Alpha.TextSecondary))
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp),
                shape = CircleShape,
                color = StashTheme.colors.buttonBackground
            ) {
                IconButton(onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onDeleteImage()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Image",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        content = { content() }
    )
}
