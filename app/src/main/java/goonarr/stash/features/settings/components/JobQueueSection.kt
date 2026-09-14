package goonarr.stash.features.settings.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Job
import goonarr.stash.core.model.JobStatus
import goonarr.stash.core.network.WebSocketState
import goonarr.stash.features.components.OutlinedCard
import goonarr.stash.util.DateFormatters.formatTimestamp
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.flow.StateFlow

private val COLLAPSED_HEIGHT = 275.dp

/**
 * A self-contained job queue section that observes the jobs flow directly.
 * Displays active jobs with a connection status badge.
 *
 * @param jobsFlow A [StateFlow] providing the current list of [Job]s.
 * @param webSocketStateFlow An optional [StateFlow] providing the current [WebSocketState].
 * @param onStopJob Callback triggered to stop a specific job by its ID.
 * @param onStopAllJobs Callback triggered to stop all currently active jobs.
 */
@Composable
fun JobQueueSection(
    jobsFlow: StateFlow<List<Job>>,
    webSocketStateFlow: StateFlow<WebSocketState>? = null,
    onStopJob: (String) -> Unit,
    onStopAllJobs: () -> Unit
) {
    // Collect jobs state here - isolates recomposition to this composable only
    val jobs by jobsFlow.collectAsState()
    val wsState = webSocketStateFlow?.collectAsState()?.value

    JobQueueContainer(
        jobs = jobs,
        webSocketState = wsState,
        onStopJob = onStopJob,
        onStopAllJobs = onStopAllJobs
    )
}

/**
 * Container for the job queue with connection status displayed at the top.
 *
 * @param jobs The list of [Job]s to display.
 * @param webSocketState The current [WebSocketState], if available.
 * @param onStopJob Callback to stop a specific job.
 * @param onStopAllJobs Callback to stop all active jobs.
 */
@Composable
private fun JobQueueContainer(
    jobs: List<Job>,
    webSocketState: WebSocketState?,
    onStopJob: (String) -> Unit,
    onStopAllJobs: () -> Unit
) {
    val haptic = LocalStashHapticFeedback.current
    val hasActiveJobs = jobs.any { !it.isCompleted }
    // Connection status badge outside and above the card
    Column(modifier = Modifier.fillMaxWidth()) {
        // Connection badge row - top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(bottom = 4.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Task Queue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Medium)
                        onStopAllJobs()
                    },
                    enabled = hasActiveJobs,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 4.dp)
                ) {
                    if (hasActiveJobs) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel All Jobs",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            webSocketState?.let { state ->
                ConnectionStatusBadge(state = state)
            }
        }
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // Content area - shrinks when empty, fixed height when has jobs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when {
                                jobs.isEmpty() -> Modifier // Wrap content when empty
                                else -> Modifier.height(COLLAPSED_HEIGHT - 48.dp)
                            }
                        )
                        .then(
                            if (jobs.isNotEmpty()) Modifier.verticalScroll(rememberScrollState()) else Modifier
                        )
                ) {
                    if (jobs.isEmpty()) {
                        Text(
                            text = "No active tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            jobs.forEach { job ->
                                key(job.id) {
                                    JobRowCompact(
                                        job = job,
                                        onStopJob = onStopJob
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

/**
 * A badge displaying the current connection status of the background task processor.
 *
 * @param state The current [WebSocketState] to display.
 */
@Composable
private fun ConnectionStatusBadge(
    state: WebSocketState
) {
    val (text, color) = when (state) {
        is WebSocketState.Connected -> "Connected" to Color(0xFF4CAF50)
        is WebSocketState.Connecting -> "Connecting..." to Color(0xFFFF9800)
        is WebSocketState.Reconnecting -> "Reconnecting (${state.attempt})..." to Color(0xFFFF9800)
        is WebSocketState.Disconnected -> "Disconnected" to Color(0xFFF44336)
        is WebSocketState.Error -> "Error" to Color(0xFFF44336)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

/**
 * A compact row representing a single [Job].
 * Displays the job's status via a colored dot, description, progress bar, subtasks, and error messages.
 *
 * @param job The [Job] data to display.
 * @param onStopJob Callback triggered when the user attempts to stop this job.
 */
@Composable
private fun JobRowCompact(job: Job, onStopJob: (String) -> Unit) {
    val haptic = LocalStashHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header: Status dot, description, status text, cancel button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colorForStatus(job.status))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Job description
            Text(
                text = job.description ?: "Unknown Task",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Progress percentage
            if (!job.isCompleted && job.progress != null) {
                Text(
                    text = "${(job.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status text
            Text(
                text = job.status.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Cancel button (reserved space even when completed)
            IconButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onStopJob(job.id)
                },
                enabled = !job.isCompleted,
                modifier = Modifier.size(24.dp)
            ) {
                if (!job.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel Job",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Progress bar
        if (!job.isCompleted && job.progress != null) {
            LinearProgressIndicator(
                progress = { job.progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // SubTasks - show all subtasks (matching iOS behavior)
        job.subTasks?.forEach { subTask ->
            Text(
                text = subTask,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Error message
        job.error?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Completion time for finished jobs (matching iOS)
        if (job.isCompleted) {
            job.endTime?.let { endTime ->
                Text(
                    text = "Completed: ${formatTimestamp(endTime)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun colorForStatus(status: JobStatus): Color {
    return when (status) {
        JobStatus.READY -> Color(0xFF2196F3) // Blue
        JobStatus.RUNNING -> Color(0xFF4CAF50) // Green
        JobStatus.STOPPING -> Color(0xFFFF9800) // Orange
        JobStatus.CANCELLED -> Color(0xFF9E9E9E) // Gray
        JobStatus.FINISHED -> Color(0xFF2196F3) // Blue
        JobStatus.FAILED -> Color(0xFFF44336) // Red
    }
}

// MARK: - Previews

/**
 * A stateless version of the Job Queue section used primarily for previews.
 *
 * @param jobs The list of jobs to display.
 * @param webSocketState The current connection state for the badge.
 * @param onStopJob Callback for stopping an individual job.
 * @param onStopAllJobs Callback for stopping all jobs.
 */
@Composable
fun JobQueueSectionPreview(
    jobs: List<Job>,
    webSocketState: WebSocketState = WebSocketState.Connected,
    onStopJob: (String) -> Unit = {},
    onStopAllJobs: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        JobQueueContainer(
            jobs = jobs,
            webSocketState = webSocketState,
            onStopJob = onStopJob,
            onStopAllJobs = onStopAllJobs
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_MultipleJobs() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = MockData.jobs,
                    webSocketState = WebSocketState.Connected
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_Scanning() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = listOf(MockData.scanningJob),
                    webSocketState = WebSocketState.Connected
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_Generation() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = listOf(MockData.generationJob),
                    webSocketState = WebSocketState.Connected
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_Empty() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = emptyList(),
                    webSocketState = WebSocketState.Connected
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_Disconnected() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = emptyList(),
                    webSocketState = WebSocketState.Disconnected
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JobQueueSectionPreview_Reconnecting() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                JobQueueSectionPreview(
                    jobs = listOf(MockData.scanningJob),
                    webSocketState = WebSocketState.Reconnecting(3)
                )
            }
        }
    }
}
