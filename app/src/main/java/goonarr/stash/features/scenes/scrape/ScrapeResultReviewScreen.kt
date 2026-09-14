package goonarr.stash.features.scenes.scrape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.performers.PerformerHeroCard
import goonarr.stash.features.scenes.scenecard.components.SmartImage
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapeResultReviewScreen(
    scrapedScene: ScrapedScene,
    currentScene: Scene?,
    taggerConfig: TaggerConfig,
    onApply: (ScrapeResultSelection) -> Unit,
    onBack: () -> Unit,
    // Made nullable for preview convenience
    viewModel: SceneScrapeViewModel? = null
) {
    // Selection state
    var useTitle by remember { mutableStateOf(scrapedScene.title != null) }
    var useDetails by remember { mutableStateOf(scrapedScene.details != null) }
    var useDate by remember { mutableStateOf(scrapedScene.date != null) }
    var useUrl by remember { mutableStateOf(scrapedScene.url != null || scrapedScene.urls?.isNotEmpty() == true) }
    var useStudio by remember { mutableStateOf(scrapedScene.studio != null) }
    var useDirector by remember { mutableStateOf(scrapedScene.director != null) }
    var useCode by remember { mutableStateOf(scrapedScene.code != null) }
    var useCover by remember { mutableStateOf((scrapedScene.image != null) && taggerConfig.setCoverImage) }
    var usePerformers by remember { mutableStateOf(scrapedScene.performers?.isNotEmpty() == true) }
    var useTags by remember { mutableStateOf((scrapedScene.tags?.isNotEmpty() == true) && taggerConfig.setTags) }
    var useStashId by remember { mutableStateOf(scrapedScene.remoteSiteId != null) }
    var appliedTags by remember(scrapedScene.tags) { mutableStateOf(scrapedScene.tags ?: emptyList()) }

    // Validation state
    var performerMatches by remember { mutableStateOf<Map<String, Performer?>>(emptyMap()) }
    var validatingPerformers by remember { mutableStateOf(true) }

    // Validate performers on load
    LaunchedEffect(scrapedScene) {
        if (viewModel != null) {
            scrapedScene.performers?.let { performers ->
                val names = performers.map { it.name }
                performerMatches = viewModel.matchScrapedPerformers(names)
            }
        }
        validatingPerformers = false
    }

    // Visibility state for staggered entry
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    val context = LocalContext.current
    val screenshotUrl = currentScene?.paths?.screenshot
    val haptic = LocalStashHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (screenshotUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(screenshotUrl)
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Review Changes") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = StashTheme.colors.complementary,
                        navigationIconContentColor = StashTheme.colors.complementary,
                        actionIconContentColor = StashTheme.colors.complementary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Success)
                                onApply(
                                    ScrapeResultSelection(
                                        useTitle = useTitle,
                                        useDetails = useDetails,
                                        useDate = useDate,
                                        useUrl = useUrl,
                                        useStudio = useStudio,
                                        useDirector = useDirector,
                                        useCode = useCode,
                                        useCover = useCover,
                                        usePerformers = usePerformers,
                                        useTags = useTags,
                                        useStashId = useStashId,
                                        tagsToApply = appliedTags
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Apply")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Images Comparison
                StaggeredEntry(visible = contentVisible, index = 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Image
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "CURRENT COVER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SmartImage(
                                    model = currentScene?.paths?.screenshot,
                                    contentDescription = "Current Cover",
                                    modifier = Modifier
                                        .aspectRatio(16f / 9f)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        // Scraped Image
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "SCRAPED COVER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StashTheme.colors.primary
                                )
                                Checkbox(
                                    checked = useCover,
                                    onCheckedChange = {
                                        haptic.perform(StashHapticFeedbackType.Light)
                                        useCover = it
                                    },
                                    enabled = scrapedScene.image != null,
                                    colors = CheckboxDefaults.colors(checkedColor = StashTheme.colors.primary)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SmartImage(
                                    model = scrapedScene.image,
                                    contentDescription = "Scraped Cover",
                                    modifier = Modifier
                                        .aspectRatio(16f / 9f)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // Metadata Fields
                StaggeredEntry(visible = contentVisible, index = 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "DESCRIPTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StashTheme.colors.primary.copy(alpha = 0.8f)
                        )

                        SelectionRow("Title", scrapedScene.title, currentScene?.title, useTitle) { useTitle = it }
                        SelectionRow("Details", scrapedScene.details, currentScene?.details, useDetails) { useDetails = it }
                        SelectionRow("Date", scrapedScene.date, currentScene?.date, useDate) { useDate = it }
                        SelectionRow("Director", scrapedScene.director, currentScene?.director, useDirector) { useDirector = it }
                        SelectionRow(
                            "Stash ID",
                            scrapedScene.remoteSiteId,
                            currentScene?.stashIds?.firstOrNull()?.stashId,
                            useStashId
                        ) {
                            useStashId = it
                        }
                        SelectionRow(
                            "Studio",
                            scrapedScene.studio?.name,
                            currentScene?.studio?.name,
                            useStudio
                        ) { useStudio = it }
                        SelectionRow("Scene Code", scrapedScene.code, currentScene?.code, useCode) { useCode = it }
                        SelectionRow("URL", scrapedScene.url, currentScene?.url, useUrl) { useUrl = it }
                    }
                }

                // Performers
                StaggeredEntry(visible = contentVisible, index = 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "PERFORMERS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StashTheme.colors.primary.copy(alpha = 0.8f)
                            )
                            Checkbox(
                                checked = usePerformers,
                                onCheckedChange = {
                                    haptic.perform(StashHapticFeedbackType.Light)
                                    usePerformers = it
                                },
                                enabled = !scrapedScene.performers.isNullOrEmpty(),
                                colors = CheckboxDefaults.colors(checkedColor = StashTheme.colors.primary)
                            )
                        }

                        if (scrapedScene.performers.isNullOrEmpty()) {
                            Text(
                                text = "No performers scraped",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            scrapedScene.performers.forEach { performer ->
                                ScrapedPerformerItem(
                                    performer = performer,
                                    localPerformer = performerMatches[performer.name],
                                    isMissing = performerMatches[performer.name] == null,
                                    onCreate = { n, img, bd, eth, c, e, h, m, f, car, tat, p, a, d, dth, hair, w, r ->
                                        viewModel?.createPerformer(
                                            // gender, urlParam
                                            n, img, bd, eth, c, e, h, m, f, car, tat, p, a, d, dth, hair, w, r,
                                            performer.gender, null
                                        )
                                        // After creation, we could re-validate, but for now just mark as found
                                        performerMatches =
                                            performerMatches + (n to Performer(n, n, imagePath = img))
                                    }
                                )
                            }
                        }
                    }
                }

                // Tags
                StaggeredEntry(visible = contentVisible, index = 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TAGS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StashTheme.colors.primary.copy(alpha = 0.8f)
                            )
                            Checkbox(
                                checked = useTags,
                                onCheckedChange = {
                                    haptic.perform(StashHapticFeedbackType.Light)
                                    useTags = it
                                },
                                enabled = !scrapedScene.tags.isNullOrEmpty(),
                                colors = CheckboxDefaults.colors(checkedColor = StashTheme.colors.primary)
                            )
                        }

                        if (appliedTags.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No tags selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                appliedTags.forEach { tag ->
                                    Surface(
                                        color = StashTheme.colors.tertiary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(StashTokens.Radius.Medium),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tag,
                                                contentDescription = null,
                                                tint = StashTheme.colors.tertiary
                                            )
                                            Text(
                                                text = tag.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptic.perform(StashHapticFeedbackType.Medium)
                                                    appliedTags = appliedTags - tag
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionRow(
    label: String,
    scrapedValue: String?,
    currentValue: String?,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    if (scrapedValue == null) return

    val haptic = LocalStashHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Surface(
            color = StashTheme.colors.primary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Current Value
                Column {
                    Text(
                        "CURRENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = currentValue ?: "None",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentValue == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                // Scraped Value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SCRAPED", style = MaterialTheme.typography.labelSmall, color = StashTheme.colors.primary.copy(alpha = 0.7f))
                        Text(scrapedValue, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            haptic.perform(StashHapticFeedbackType.Light)
                            onToggle(it)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = StashTheme.colors.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun ScrapedPerformerItem(
    performer: ScrapedPerformer,
    localPerformer: Performer? = null,
    isMissing: Boolean,
    onCreate: (
        String,
        String?,
        String?,
        String?,
        String?,
        String?,
        Int?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        Int?,
        Int?
    ) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val haptic = LocalStashHapticFeedback.current

// Map ScrapedPerformer to Performer domain model for HeroCard if no local one exists
    val displayPerformer = localPerformer ?: remember(performer) {
        Performer(
            id = "scraped",
            name = performer.name,
            gender = performer.gender,
            imagePath = performer.images?.firstOrNull(),
            sceneCount = 0
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        PerformerHeroCard(
            performer = displayPerformer,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            height = 360.dp
        )

        // Status/Action Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMissing) {
                    Text("Missing", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StashTheme.colors.primary)
                    ) {
                        Text("Create", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Exists", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Icon(Icons.Default.Check, contentDescription = "Exists", tint = Color.Green, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        PerformerCreateDialog(
            performer = performer,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, img, bday, eth, country, eye, height, meas, fake, career, tat, pierce, alias, det, death, hair, w, r ->
                haptic.perform(StashHapticFeedbackType.Success)
                onCreate(name, img, bday, eth, country, eye, height, meas, fake, career, tat, pierce, alias, det, death, hair, w, r)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PerformerCreateDialog(
    performer: ScrapedPerformer,
    onDismiss: () -> Unit,
    onCreate: (
        String,
        String?,
        String?,
        String?,
        String?,
        String?,
        Int?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        String?,
        Int?,
        Int?
    ) -> Unit
) {
// Basic fields
    var name by remember { mutableStateOf(performer.name) }
    var birthDate by remember { mutableStateOf("") } // Date picker ideally

// In a real app we'd expose all fields. For POC, let's do essentials.

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Performer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                // Pass scraped stats if available, and override name/birthdate
                onCreate(
                    name,
                    performer.images?.firstOrNull(),
                    birthDate.ifEmpty { null },
                    // Fill others as needed
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                )
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
