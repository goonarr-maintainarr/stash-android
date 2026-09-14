package goonarr.stash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a background job in the Stash system.
 *
 * @property id Unique identifier for the job.
 * @property status Current status of the job (e.g., RUNNING, FINISHED).
 * @property progress Progress percentage (0.0 to 1.0).
 */
@Serializable
data class Job(
    val id: String,
    val addTime: String? = null,
    val description: String? = null,
    val status: JobStatus,
    val progress: Double? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val error: String? = null,
    val subTasks: List<String>? = null
) {
    /**
     * Helper to check if the job has reached a terminal state (Finished, Failed, or Cancelled).
     */
    val isCompleted: Boolean
        get() = status == JobStatus.FINISHED || status == JobStatus.FAILED || status == JobStatus.CANCELLED
}

/**
 * Enumeration of possible job statuses.
 */
@Serializable
enum class JobStatus {
    @SerialName("READY")
    READY,

    @SerialName("RUNNING")
    RUNNING,

    @SerialName("STOPPING")
    STOPPING,

    @SerialName("CANCELLED")
    CANCELLED,

    @SerialName("FINISHED")
    FINISHED,

    @SerialName("FAILED")
    FAILED;

    /**
     * User-friendly display name for the status.
     */
    val displayName: String
        get() = when (this) {
            READY -> "Ready"
            RUNNING -> "Running"
            STOPPING -> "Stopping"
            CANCELLED -> "Cancelled"
            FINISHED -> "Finished"
            FAILED -> "Failed"
        }
}
