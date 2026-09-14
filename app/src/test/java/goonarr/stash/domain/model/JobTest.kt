package goonarr.stash.domain.model

import goonarr.stash.core.model.Job
import goonarr.stash.core.model.JobStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobTest {

    @Test
    fun `isCompleted returns true for finished states`() {
        assertTrue(createJob(JobStatus.FINISHED).isCompleted)
        assertTrue(createJob(JobStatus.FAILED).isCompleted)
        assertTrue(createJob(JobStatus.CANCELLED).isCompleted)
    }

    @Test
    fun `isCompleted returns false for active states`() {
        assertFalse(createJob(JobStatus.READY).isCompleted)
        assertFalse(createJob(JobStatus.RUNNING).isCompleted)
        assertFalse(createJob(JobStatus.STOPPING).isCompleted)
    }

    private fun createJob(status: JobStatus): Job {
        return Job(
            id = "1",
            status = status
        )
    }
}
