# Stash Playback Tracking Behavior Specification

This document defines the exact playback tracking behavior used by Stash-GO (web UI) that must be replicated in iOS and Android apps.

## Table of Contents

- [Overview](#overview)
- [Tracking Rules](#tracking-rules)
- [Detailed Behavior](#detailed-behavior)
- [Implementation Requirements](#implementation-requirements)
- [Edge Cases](#edge-cases)
- [Testing Checklist](#testing-checklist)

---

## Overview

Stash playback tracking is **NOT automatic**. The client must explicitly track playback and send updates to the server based on specific rules.

### Core Principles

1. **Tracking only happens during active playback** (not while paused or scrubbing)
2. **Updates are sent every 10 seconds** of accumulated play time
3. **Play count increments once per session** when minimum threshold is reached
4. **Resume time resets to 0** when video reaches 98% completion

---

## Tracking Rules

### When Tracking Starts

Tracking begins when the video player fires a **`playing`** event:
- User presses play
- Video starts after buffering
- User resumes from pause

### When Tracking Stops

Tracking stops (and sends a final update) when these events occur:
- `pause` - User pauses playback
- `waiting` - Video is buffering
- `stalled` - Playback stalled due to network issues
- `ended` - Video reached the end
- `dispose` - Player is destroyed/closed

### When Updates Are Sent

Updates are sent to the server:
- **Every 10 seconds** of accumulated playback time
- **When tracking stops** (pause, buffer, close) if there's accumulated time

### What Does NOT Trigger Updates

These actions do **NOT** send updates:
- Opening the scene
- Scrubbing/seeking to a different position
- Closing the scene without playing
- Playing for less than 10 seconds total

---

## Detailed Behavior

### Scenario 1: Opening a Scene

```
1. User navigates to scene detail
2. App queries scene.resume_time from server
3. Player seeks to resume_time position
4. ❌ NO API calls sent
5. ❌ NO tracking started
```

**Result:** Player shows scene at last watched position, but nothing is saved.

---

### Scenario 2: Scrubbing Without Playing

```
1. User drags scrubber to 5:00
2. Player seeks to 5:00
3. User views thumbnail/preview
4. User closes scene
5. ❌ NO API calls sent
6. ❌ resume_time remains unchanged
```

**Result:** Scrubbing alone does not update resume time.

---

### Scenario 3: Playing for Less Than 10 Seconds

```
1. User presses play
2. Tracking starts (internal counter begins)
3. User plays for 7 seconds
4. User pauses or closes scene
5. ❌ NO API calls sent (under 10-second threshold)
6. ❌ resume_time unchanged
7. ❌ play_history unchanged
8. ❌ play_count unchanged
```

**Result:** Short playback sessions (< 10 seconds) are not tracked.

---

### Scenario 4: Playing for 10 Seconds (First Update)

```
1. User presses play
2. Tracking starts
3. After 10 seconds of playback:
   ✅ sceneSaveActivity called
      - resumeTime: current player position
      - playDuration: 10.0
   ✅ sceneIncrementPlayCount called (first time only)
   ✅ play_history updated with current timestamp
```

**GraphQL Mutations Sent:**
```graphql
# Mutation 1: Increment play count (once per session)
mutation {
  sceneIncrementPlayCount(id: "123")
}

# Mutation 2: Save activity
mutation {
  sceneSaveActivity(
    id: "123"
    resume_time: 145.5
    playDuration: 10.0
  )
}
```

**Result:** Scene is now marked as "watched" with play count +1 and timestamp in history.

---

### Scenario 5: Playing for 20 Seconds (Second Update)

```
1. User continues playing from 10-second mark
2. After another 10 seconds (20 total):
   ✅ sceneSaveActivity called again
      - resumeTime: current player position
      - playDuration: 10.0 (only the new 10 seconds)
   ❌ sceneIncrementPlayCount NOT called (already incremented)
```

**GraphQL Mutation Sent:**
```graphql
mutation {
  sceneSaveActivity(
    id: "123"
    resume_time: 175.2
    playDuration: 10.0
  )
}
```

**Result:** Resume time updated, but play count stays the same.

---

### Scenario 6: Playing for 25 Seconds Then Pausing

```
1. User plays for 25 seconds total
2. Updates sent at 10s and 20s marks
3. User pauses
4. Tracking stops
5. Final update sent:
   ✅ sceneSaveActivity called
      - resumeTime: current player position
      - playDuration: 5.0 (remaining accumulated time)
```

**GraphQL Mutation Sent:**
```graphql
mutation {
  sceneSaveActivity(
    id: "123"
    resume_time: 180.3
    playDuration: 5.0
  )
}
```

**Result:** Final 5 seconds are saved before pause.

---

### Scenario 7: Watching to 98% Completion

```
1. User plays video to 98% or more
2. Resume time is set to 0 (reset)
3. ✅ sceneSaveActivity called
      - resumeTime: 0 (reset, not current position)
      - playDuration: accumulated seconds
```

**GraphQL Mutation Sent:**
```graphql
mutation {
  sceneSaveActivity(
    id: "123"
    resume_time: 0
    playDuration: 10.0
  )
}
```

**Result:** Next time user opens scene, it starts from beginning (not 98%).

---

## Implementation Requirements

### Constants

```kotlin
// Android/Kotlin
const val TRACKING_INTERVAL_SECONDS = 1.0  // Check playback every second
const val SYNC_INTERVAL_SECONDS = 10.0     // Send updates every 10 seconds
const val COMPLETION_THRESHOLD = 0.98      // 98% = considered complete
const val MINIMUM_PLAY_PERCENT = 0.0       // Increment play count at 0%
```

```swift
// iOS/Swift
let trackingIntervalSeconds: TimeInterval = 1.0
let syncIntervalSeconds: TimeInterval = 10.0
let completionThreshold: Double = 0.98
let minimumPlayPercent: Double = 0.0
```

---

### Tracking State

Your tracker must maintain:

```kotlin
// Android
class PlaybackTracker {
    private var totalPlayDuration: Double = 0.0      // Total seconds played this session
    private var currentPlayDuration: Double = 0.0    // Seconds since last sync
    private var playCountIncremented: Boolean = false // Only increment once
    private var isTracking: Boolean = false
}
```

```swift
// iOS
class PlaybackTracker {
    private var totalPlayDuration: TimeInterval = 0
    private var currentPlayDuration: TimeInterval = 0
    private var playCountIncremented = false
    private var timeObserver: Any?
}
```

---

### Tracking Logic

#### Every Second (While Playing)

```kotlin
fun trackPlayback() {
    // Only track if player is actually playing
    if (!player.isPlaying) return
    
    totalPlayDuration += 1.0
    currentPlayDuration += 1.0
    
    // Check if we should send update
    if (totalPlayDuration % 10.0 == 0.0) {
        sendActivity()
    }
}
```

#### Send Activity Update

```kotlin
fun sendActivity() {
    if (currentPlayDuration <= 0) return
    
    val currentTime = player.currentPosition / 1000.0 // Convert ms to seconds
    val duration = player.duration / 1000.0
    
    if (duration <= 0 || !duration.isFinite()) return
    
    val percentCompleted = (currentTime / duration) * 100
    val percentPlayed = (totalPlayDuration / duration) * 100
    
    // Increment play count once when threshold reached
    if (!playCountIncremented && percentPlayed >= minimumPlayPercent) {
        incrementPlayCount()
        playCountIncremented = true
    }
    
    // Reset resume time if video is 98% or more complete
    val resumeTime = if (percentCompleted >= 98) 0.0 else currentTime
    
    // Send to server
    saveActivity(resumeTime, currentPlayDuration)
    
    // Reset accumulated duration
    currentPlayDuration = 0.0
}
```

---

### Player Event Handlers

#### Android (ExoPlayer)

```kotlin
player.addListener(object : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            startTracking()
        } else {
            stopTracking()
        }
    }
    
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_ENDED -> stopTracking()
            Player.STATE_BUFFERING -> stopTracking() // Treat buffering as pause
        }
    }
})
```

#### iOS (AVPlayer)

```swift
// Use addPeriodicTimeObserver for tracking
timeObserver = player.addPeriodicTimeObserver(
    forInterval: CMTime(seconds: 1.0, preferredTimescale: 600),
    queue: .main
) { [weak self] _ in
    self?.trackPlayback()
}

// Observe playback state
player.observe(\.rate) { [weak self] player, _ in
    if player.rate > 0 {
        // Playing
        self?.startTracking()
    } else {
        // Paused
        self?.stopTracking()
    }
}
```

---

### Starting Tracking

```kotlin
fun startTracking() {
    if (isTracking) return
    isTracking = true
    // Timer/observer already handles periodic checks
}
```

---

### Stopping Tracking

```kotlin
fun stopTracking() {
    if (!isTracking) return
    isTracking = false
    
    // Send final update with any accumulated time
    if (currentPlayDuration > 0) {
        sendActivity()
    }
}
```

---

### Resuming Playback

When loading a scene, seek to the stored resume time:

```kotlin
fun loadScene(scene: Scene) {
    // Seek to resume time if available
    scene.resumeTime?.let { resumeTime ->
        if (resumeTime > 0) {
            player.seekTo((resumeTime * 1000).toLong()) // Convert to ms
        }
    }
    
    // Initialize tracker
    playbackTracker = PlaybackTracker(
        sceneId = scene.id,
        player = player,
        repository = repository
    )
}
```

```swift
func loadScene(_ scene: Scene) {
    // Seek to resume time if available
    if let resumeTime = scene.resumeTime, resumeTime > 0 {
        player.seek(to: CMTime(seconds: resumeTime, preferredTimescale: 600))
    }
    
    // Setup tracking
    setupPlaybackTracking(for: scene)
}
```

---

## Edge Cases

### 1. User Seeks During Playback

```
Behavior: Continue tracking from new position
- Seeking does NOT stop tracking
- Resume time will be updated to new position at next sync
- Play duration continues accumulating
```

**Example:**
```
1. User plays from 1:00 to 1:10 (10 seconds)
   - First sync: resumeTime=1:10, playDuration=10
2. User seeks to 5:00
3. User plays from 5:00 to 5:10 (10 more seconds)
   - Second sync: resumeTime=5:10, playDuration=10
```

---

### 2. Network Failure

```
Behavior: Tracking continues, mutations fail silently
- Don't block playback on network errors
- Optionally implement retry logic
- Play count and history may be lost for this session
```

---

### 3. App Backgrounded

```
Behavior: Stop tracking when app backgrounds (except PiP)
- Call stopTracking() in onPause (Android) or appDidEnterBackground (iOS)
- Resume tracking when app comes back to foreground
- Send final update before backgrounding
```

**Android:**
```kotlin
override fun onPause() {
    super.onPause()
    if (!isInPictureInPictureMode) {
        playbackTracker.stopTracking()
    }
}

override fun onResume() {
    super.onResume()
    if (player.isPlaying) {
        playbackTracker.startTracking()
    }
}
```

---

### 4. Player Destroyed Before 10 Seconds

```
Behavior: No updates sent
- User plays for 8 seconds, closes app
- No resume time update
- No play history entry
- This is correct behavior per spec
```

---

### 5. Multiple Viewing Sessions

```
Each time user opens and plays the scene:
- Play count increments by 1
- New timestamp added to play_history
- Resume time is updated throughout playback
```

**Example:**
```
Session 1: Play for 30 seconds → play_count=1, history=["2024-01-15T10:00:00Z"]
Session 2: Play for 20 seconds → play_count=2, history=["2024-01-15T10:00:00Z", "2024-01-15T14:30:00Z"]
```

---

### 6. Rapid Play/Pause

```
Behavior: Each play/pause cycle
- Playing accumulates time
- Pausing sends final update
- Resuming starts accumulating again
- All accumulated time counts toward the 10-second threshold
```

**Example:**
```
1. Play 5 seconds → pause (no update, < 10s threshold)
2. Play 6 seconds → pause (update sent with 11s total, resumeTime updated)
```

---

## Testing Checklist

### Basic Functionality

- [ ] Opening scene seeks to resume_time
- [ ] Scrubbing without playing doesn't update resume_time
- [ ] Playing for < 10 seconds sends no updates
- [ ] Playing for 10+ seconds sends update
- [ ] Play count increments exactly once per session
- [ ] Updates sent every 10 seconds during playback
- [ ] Pausing sends final update with remaining seconds
- [ ] Closing player stops tracking

### Resume Time Behavior

- [ ] Resume time updates to current position during playback
- [ ] Resume time resets to 0 at 98% completion
- [ ] Resume time doesn't change when scrubbing without playing
- [ ] Seeking during playback updates resume time at next sync

### Play Count & History

- [ ] Play count increments after first 10 seconds
- [ ] Play count doesn't increment again in same session
- [ ] Play history adds timestamp at first 10-second sync
- [ ] Multiple sessions each add new play count and timestamp
- [ ] Opening scene without playing doesn't affect history

### Edge Cases

- [ ] Network failures don't crash app
- [ ] Backgrounding app stops tracking
- [ ] Foreground app resumes tracking if playing
- [ ] Picture-in-Picture continues tracking
- [ ] Player destruction sends final update
- [ ] Buffering stops tracking temporarily
- [ ] Rapid play/pause accumulates time correctly

---

## Summary Table

| User Action | Resume Time | Play Count | Play History | Notes |
|-------------|-------------|------------|--------------|-------|
| Open scene | Seek to stored value | No change | No change | Just seeks, no API calls |
| Scrub without playing | No change | No change | No change | Seeking alone doesn't track |
| Play < 10 seconds | No change | No change | No change | Below tracking threshold |
| Play 10 seconds | ✅ Updated | ✅ +1 | ✅ Timestamp added | First sync |
| Play 20 seconds | ✅ Updated | No change | No change | Second sync |
| Play 30 seconds | ✅ Updated | No change | No change | Third sync |
| Pause during playback | ✅ Updated | No change | No change | Final sync sent |
| Watch to 98% | ✅ Reset to 0 | No change | No change | Auto-reset for replay |
| Close without playing | No change | No change | No change | No tracking occurred |

---

## API Reference

### Save Activity

```graphql
mutation SaveSceneActivity($sceneId: ID!, $resumeTime: Float, $playDuration: Float) {
  sceneSaveActivity(
    id: $sceneId
    resume_time: $resumeTime
    playDuration: $playDuration
  )
}
```

### Increment Play Count

```graphql
mutation IncrementPlayCount($sceneId: ID!) {
  sceneIncrementPlayCount(id: $sceneId)
}
```

---

## Implementation Checklist

### Android
- [ ] PlaybackTracker class with state management
- [ ] ExoPlayer.Listener implementation
- [ ] Timer for 1-second intervals
- [ ] Repository mutations (saveActivity, incrementPlayCount)
- [ ] Lifecycle handling (onPause/onResume)
- [ ] PiP support

### iOS
- [ ] PlaybackTracker class with AVPlayer integration
- [ ] AVPlayerItemTimeObserver for periodic updates
- [ ] Rate observer for play/pause detection
- [ ] Repository mutations
- [ ] App lifecycle observers
- [ ] Background playback support

---

## Notes

- All timing is based on **actual playback time**, not wall-clock time
- Seeking/scrubbing does NOT stop or reset tracking
- The 10-second threshold is **cumulative** (5s + 5s = tracked)
- Updates are sent in the **background** (non-blocking)
- Network failures should be handled gracefully
- Tracking should work offline and sync when connection returns (optional enhancement)
