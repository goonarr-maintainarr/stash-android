package goonarr.stash.util

import android.graphics.Bitmap
import android.graphics.Color
import goonarr.stash.core.model.Job
import goonarr.stash.core.model.JobStatus
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.PerformerTag
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.core.model.scraper.ScrapedTag
import goonarr.stash.core.model.scraper.StashBox

object MockData {
    val studio = Studio(
        id = "1",
        name = "Nebula Productions",
        imagePath = "https://example.com/studio.jpg",
        sceneCount = 120,
        performerCount = 45,
        details = "A legendary studio known for high-quality productions and creative storytelling.",
        favorite = false,
        stashIds = listOf(
            StashID(endpoint = "https://stashdb.org/graphql", stashId = "studio-123")
        )
    )

    val fullStudio = Studio(
        id = "2",
        name = "Midnight Cinema Elite",
        imagePath = "https://example.com/midnight.jpg",
        sceneCount = 250,
        imageCount = 15,
        galleryCount = 5,
        performerCount = 82,
        groupCount = 2,
        details = "Midnight Cinema Elite has been at the forefront of the industry for over a decade, " +
            "specializing in cinematic experiences with high production values and talented performers.",
        rating100 = 95,
        favorite = true,
        ignoreAutoTag = true,
        aliases = listOf("MCE", "Midnight Elite", "Cinemax"),
        urls = listOf(
            "https://midnightcinema.com",
            "https://twitter.com/midnightcinema",
            "https://instagram.com/midnightcinema"
        ),
        createdAt = "2010-01-01T00:00:00Z",
        updatedAt = "2024-01-15T12:00:00Z",
        stashIds = listOf(
            StashID(endpoint = "https://stashdb.org/graphql", stashId = "studio-123"),
            StashID(endpoint = "https://theporndb.net/graphql", stashId = "studio-abc")
        )
    )

    private val realisticStudioNames = listOf(
        "Nebula Productions", "Crimson Cinema", "Azure Entertainment", "Golden Gate Studios",
        "Emerald Media", "Midnight Films", "Velocity Pictures", "Horizon Audio Visual",
        "Prism Studios", "Titan Works", "Infinity Digital", "Starlight Media",
        "Apex Productions", "Zenith Cinema", "Oracle Films", "Pulse Media",
        "Radiant Studios", "Shadow Work", "Elysium Entertainment", "Nova Pictures"
    )

    val studios = realisticStudioNames.mapIndexed { index, name ->
        studio.copy(
            id = "${index + 1}",
            name = name,
            sceneCount = (index + 1) * 15,
            favorite = index % 3 == 0
        )
    }

    val performer = Performer(
        id = "1",
        name = "Mock Performer",
        imagePath = "https://example.com/performer.jpg",
        country = "USA",
        birthdate = "1990-01-01",
        sceneCount = 10,
        oCounter = 5
    )

    val fullPerformer = Performer(
        id = "2",
        name = "Jane Doe",
        disambiguation = "The Actress",
        urls = listOf("https://twitter.com/janedoe"),
        gender = "Female",
        birthdate = "1995-05-15",
        deathDate = null,
        country = "USA",
        ethnicity = "Caucasian",
        eyeColor = "Blue",
        hairColor = "Blonde",
        heightCm = 170,
        weight = 55,
        measurements = "34-24-34",
        fakeTits = "No",
        careerLength = "2015-2023",
        tattoos = "Butterfly on shoulder",
        piercings = "Navel",
        aliasList = listOf("Jane", "J. Doe"),
        favorite = true,
        imagePath = "https://example.com/jane.jpg",
        details = "A highly acclaimed performer with a versatile portfolio...",
        sceneCount = 150,
        imageCount = 200,
        galleryCount = 50,
        oCounter = 42,
        rating100 = 85,
        createdAt = "2015-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z",
        stashIds = listOf(
            StashID(endpoint = "https://stashdb.org/graphql", stashId = "performer-123"),
            StashID(endpoint = "https://iafd.com", stashId = "Jane Doe")
        ),
        tags = listOf(
            PerformerTag("t1", "Model"),
            PerformerTag("t2", "Feature"),
            PerformerTag("t3", "Award Winner")
        )
    )

    val scene = Scene(
        id = "1",
        title = "Mock Scene Title with a slightly longer name to test wrapping",
        studio = studio,
        date = "2023-12-25",
        details = "A beautifully shot scene featuring stunning cinematography and natural chemistry. " +
            "This mock scene demonstrates all the metadata fields available in the app.",
        performers = listOf(performer),
        files = listOf(
            SceneFile(
                path = "/media/videos/Mock.Studio.23.12.25.Scene.Title.XXX.2160p.MP4-RELEASE.mp4",
                duration = 2847.0,
                width = 3840,
                height = 2160,
                videoCodec = "hevc",
                audioCodec = "aac",
                size = 8_589_934_592L
            )
        ),
        paths = ScenePaths(screenshot = "https://example.com/screenshot.jpg"),
        sceneMarkers = listOf(
            SceneMarker(
                id = "m1",
                title = "Opening Scene",
                seconds = 30.0,
                primaryTag = Tag(id = "t1", name = "Passionate"),
                tags = listOf(
                    Tag(id = "t1", name = "Passionate"),
                    Tag(id = "t2", name = "Romantic")
                )
            ),
            SceneMarker(
                id = "m2",
                title = "First Position",
                seconds = 420.0,
                tags = listOf(
                    Tag(id = "t3", name = "4K"),
                    Tag(id = "t4", name = "High Production")
                )
            ),
            SceneMarker(
                id = "m3",
                title = "Transition",
                seconds = 1050.0,
                primaryTag = Tag(id = "t5", name = "Natural Lighting")
            ),
            SceneMarker(
                id = "m4",
                title = "Climax",
                seconds = 2250.0,
                tags = listOf(
                    Tag(id = "t1", name = "Passionate"),
                    Tag(id = "t3", name = "4K"),
                    Tag(id = "t6", name = "Outdoor")
                )
            )
        ),
        tags = listOf(
            Tag(id = "t1", name = "Passionate"),
            Tag(id = "t2", name = "Romantic"),
            Tag(id = "t3", name = "4K"),
            Tag(id = "t4", name = "High Production"),
            Tag(id = "t5", name = "Natural Lighting")
        ),
        oCounter = 3,
        resumeTime = 300.0,
        oHistory = listOf(
            "2023-12-25T14:30:00Z",
            "2023-12-20T21:15:00Z",
            "2023-12-15T19:45:00Z"
        ),
        playHistory = listOf(
            "2023-12-25T14:00:00Z",
            "2023-12-20T21:00:00Z",
            "2023-12-15T19:30:00Z",
            "2023-12-10T22:00:00Z"
        ),
        // ~47 minutes total watch time
        playDuration = 2847.5,
        stashIds = listOf(
            StashID(endpoint = "https://stashdb.org/graphql", stashId = "abc123-def456-ghi789"),
            StashID(endpoint = "https://theporndb.net/graphql", stashId = "xyz789-uvw456")
        ),
        director = "John Director",
        code = "MS-2312-25",
        url = "https://example.com/scene/mock-scene",
        urls = listOf(
            "https://example.com/scene/mock-scene",
            "https://stashdb.org/scenes/abc-123",
            "https://theporndb.net/scenes/xyz-789"
        ),
        createdAt = "2023-12-01T10:00:00Z",
        updatedAt = "2023-12-25T15:00:00Z"
    )

    val performers = (1..12).map { index ->
        performer.copy(
            id = "$index",
            name = if (index == 1) "Jane Doe" else "Performer $index",
            country = listOf("USA", "UK", "Canada", "Japan", "Germany")[index % 5],
            // Every 3rd performer is a favorite
            favorite = index % 3 == 0,
            // Varying ratings: 5 stars, 4 stars, 3 stars, or no rating
            rating100 = when {
                index % 4 == 0 -> 100
                index % 4 == 1 -> 80
                index % 4 == 2 -> 60
                else -> null
            }
        )
    }

    val scenes = (1..15).map { index ->
        scene.copy(
            id = "$index",
            title = "Mock Scene #$index - A generated title for preview testing",
            date = "2023-${(index % 12) + 1}-01",
            oCounter = if (index % 3 == 0) index else 0
        )
    }

    val tag = Tag(
        id = "1",
        name = "Mock Tag",
        sceneCount = 42,
        aliases = listOf("Alias 1", "Alias 2"),
        imagePath = null,
        sceneMarkerCount = 5,
        imageCount = 10,
        galleryCount = 2,
        performerCount = 1,
        parentCount = 0,
        childCount = 0,
        favorite = true,
        ignoreAutoTag = false,
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z"
    )

    private val realisticTagNames = listOf(
        "Passionate", "Romantic", "Intense", "Close-up", "POV", "Atmospheric",
        "Cinematic", "Steadycam", "High Contrast", "Moody", "Energetic",
        "Playful", "Outdoor", "Indoor", "Studio", "Nature", "Urban",
        "Classic", "Modern", "Minimalist"
    )

    val tags = realisticTagNames.mapIndexed { index, name ->
        tag.copy(
            id = "${index + 1}",
            name = name,
            sceneCount = (index + 1) * 15
        )
    }

    val previewTag = tag.copy(
        name = "Outdoor",
        sceneCount = 153,
        description = "Scenes filmed outdoors in natural light.",
        favorite = false
    )

    val previewScenes = (1..20).map { index ->
        scene.copy(
            id = "$index",
            title = if (index % 2 == 0) "A Day in the Sun - Scene $index" else "Nature Walk - Part $index",
            date = "2023-${(index % 12) + 1}-01",
            oCounter = if (index % 3 == 0) index else 0,
            studio = studios[index % studios.size],
            performers = performers.take((index % 3) + 1),
            // Use different aspect ratios mock placeholders if we had them,
            // but for now varied titles and metadata is good.
            details = "A nice outdoor scene with natural lighting."
        )
    }

    // Mock Job data for Settings previews
    val scanningJob = Job(
        id = "job-scan",
        addTime = "2024-01-14T10:00:00Z",
        description = "Scanning library: /media/videos",
        status = JobStatus.RUNNING,
        progress = null,
        startTime = "2024-01-14T10:00:05Z",
        endTime = null,
        error = null,
        subTasks = listOf(
            "Searching for new files...",
            "Processing: Scene_042_4K.mp4",
            "Extracting metadata...",
            "Found 12 new files"
        )
    )

    val generationJob = Job(
        id = "job-gen",
        addTime = "2024-01-14T09:45:00Z",
        description = "Generating Sprites & Previews",
        status = JobStatus.RUNNING,
        progress = 0.32,
        startTime = "2024-01-14T09:45:10Z",
        endTime = null,
        error = null,
        subTasks = listOf(
            "Task: Preview Segments (14/150 items)",
            "Task: Transcode Previews (8/150 items)",
            "Task: Image Sprites (32/150 items)"
        )
    )

    val taggingJob = Job(
        id = "job-tag",
        addTime = "2024-01-14T09:00:00Z",
        description = "Auto-Tagging Scenes",
        status = JobStatus.FINISHED,
        progress = 1.0,
        startTime = "2024-01-14T09:00:02Z",
        endTime = "2024-01-14T09:15:30Z",
        error = null,
        subTasks = null
    )

    val cleanupJob = Job(
        id = "job-cleanup",
        addTime = "2024-01-14T08:30:00Z",
        description = "Cleaning up orphaned files",
        status = JobStatus.FINISHED,
        progress = 1.0,
        startTime = "2024-01-14T08:30:05Z",
        endTime = "2024-01-14T08:32:00Z",
        error = null,
        subTasks = null
    )

    val failedIdentifyJob = Job(
        id = "job-identify",
        addTime = "2024-01-14T11:00:00Z",
        description = "Identify Scenes (Stashbox)",
        status = JobStatus.FAILED,
        progress = 0.15,
        startTime = "2024-01-14T11:00:10Z",
        endTime = "2024-01-14T11:05:00Z",
        error = "Network Error: Unable to reach stashbox.org. Please check your internet connection and API key.",
        subTasks = listOf("Querying StashDB...")
    )

    val runningJob = scanningJob
    val finishedJob = taggingJob
    val failedJob = failedIdentifyJob

    val jobs = listOf(scanningJob, generationJob, taggingJob, cleanupJob, failedIdentifyJob)

    val mockStashBoxes = listOf(
        StashBox("1", "StashDB", "https://stashdb.org/graphql"),
        StashBox("2", "ThePornDB", "https://theporndb.net/graphql")
    )

    val mockScrapedResults = listOf(
        ScrapedScene(
            title = "Scene Title From StashDB",
            details = "Scene description scraped from external database. " +
                "This is a realistic looking description that might be longer than a single line.",
            date = "2024-01-15",
            studio = ScrapedStudio(name = "Studio Name"),
            performers = listOf(
                ScrapedPerformer(
                    name = "Performer One", gender = "Female",
                    images = null
                ),
                ScrapedPerformer(
                    name = "Performer Two", gender = "Male",
                    images = null
                )
            ),
            tags = listOf(
                ScrapedTag(name = "Tag One"),
                ScrapedTag(name = "Tag Two")
            ),
            image = "https://example.com/image.jpg",
            remoteSiteId = "stashdb-12345",
            director = "Director Name",
            code = "CODE-123",
            url = "https://stashdb.org/scenes/12345",
            urls = listOf("https://stashdb.org/scenes/12345")
        )
    )

    val mockScrapedPerformers = listOf(
        ScrapedPerformer(
            name = "Scraped Performer One",
            disambiguation = "The Winner",
            gender = "Female",
            birthdate = "1992-04-20",
            ethnicity = "Caucasian",
            country = "USA",
            eyeColor = "Green",
            hairColor = "Red",
            height = "165cm",
            weight = "52kg",
            measurements = "32B-24-34",
            fakeTits = "No",
            careerLength = "2012-Present",
            tattoos = "Snake on arm",
            piercings = "Ears",
            aliases = "Performer 1, P1",
            details = "Award winning performer with over 10 years of experience in the industry.",
            images = listOf("https://example.com/p1_1.jpg", "https://example.com/p1_2.jpg"),
            tags = listOf(ScrapedTag("Model"), ScrapedTag("Feature")),
            remoteSiteId = "sb-12345"
        ),
        ScrapedPerformer(
            name = "Another Scraped Performer",
            gender = "Male",
            country = "Canada",
            images = null
        )
    )

    val mockScrapedStudios = listOf(
        ScrapedStudio(
            name = "Scraped Studio One",
            urls = listOf("https://studio1.com"),
            parent = ScrapedStudio(name = "Parent Studio MegaCorp"),
            image = "https://example.com/logo1.jpg",
            remoteSiteId = "stud-111",
            details = "A premier studio known for high budget productions.",
            tags = listOf(ScrapedTag("High Quality"), ScrapedTag("4K"))
        ),
        ScrapedStudio(
            name = "Indie Studio X",
            urls = listOf("https://indiestudio.x"),
            image = null,
            details = "Experimental content from up and coming directors."
        )
    )

    /**
     * Creates a simple solid color placeholder bitmap for Compose previews.
     */
    val placeholderBitmap: Bitmap by lazy {
        Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.DKGRAY)
        }
    }
}
