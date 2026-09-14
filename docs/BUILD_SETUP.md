# Build Configuration - Stable Setup

## ✅ Working Configuration

After resolving compatibility issues, the project now uses this stable configuration:

### Core Versions
- **Gradle**: 8.2.1
- **Android Gradle Plugin (AGP)**: 8.2.2
- **Kotlin**: 1.9.22
- **KSP**: 1.9.22-1.0.17
- **Compose Compiler**: 1.5.10

### SDK Configuration
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34
- **Java**: 17
- **Kotlin JVM Target**: 17

### Major Libraries
- **Compose BOM**: 2024.12.01
- **Hilt**: 2.50
- **Ktor**: 3.0.2
- **Room**: 2.6.1
- **Coil**: 2.7.0
- **ExoPlayer (Media3)**: 1.5.0
- **OkHttp**: 4.12.0
- **Coroutines**: 1.9.0

## Version Compatibility Matrix

| Component | Version | Requirement |
|-----------|---------|-------------|
| AGP 8.2.2 | Requires | Gradle 8.0 - 8.3 |
| Kotlin 1.9.22 | Compatible with | Gradle 8.2.1 |
| Hilt 2.50 | Requires | AGP 7.0+ |
| Compose 1.5.10 | Compatible with | Kotlin 1.9.22 |
| KSP 1.9.22-1.0.17 | Matches | Kotlin 1.9.22 |

## Why These Versions?

### Kotlin 1.9.22 (not 2.0+)
- Kotlin 2.0+ requires newer Gradle/AGP versions
- The Compose Compiler plugin is only available in Kotlin 2.0+
- For Kotlin 1.9.x, we use `composeOptions.kotlinCompilerExtensionVersion`
- 1.9.22 is stable and well-tested with AGP 8.2.2

### AGP 8.2.2
- Requires Gradle 8.0 - 8.3
- Compatible with Kotlin 1.9.x
- Fully supports compileSdk 34
- Stable release with good tooling support

### Gradle 8.2.1
- Matches AGP 8.2.2 requirements
- Stable and fast
- Good IDE integration

### Hilt 2.50
- Latest version compatible with AGP 8.2.2
- Hilt 2.51+ requires AGP 8.4+
- Full KSP support

## Build Commands

### Verify Configuration
```bash
./gradlew --version
./gradlew tasks
```

### Clean Build
```bash
./gradlew clean build
```

### Sync Dependencies
```bash
./gradlew --refresh-dependencies
```

### Stop All Daemons
```bash
./gradlew --stop
```

## Troubleshooting

### Issue: "getUseClasspathSnapshot" Error
**Cause**: Version incompatibility between Gradle, Kotlin, and AGP

**Solution**: Use the stable version matrix above

### Issue: Hilt Requires AGP 8.4+
**Cause**: Using Hilt 2.51+ with AGP 8.2.2

**Solution**: Downgrade to Hilt 2.50

### Issue: KSP Version Mismatch
**Cause**: KSP version doesn't match Kotlin version

**Solution**: Use KSP 1.9.22-1.0.17 with Kotlin 1.9.22

### Clear Cache and Rebuild
```bash
./gradlew --stop
rm -rf .gradle build app/build
./gradlew clean build
```

## Future Upgrades

To upgrade to newer versions in the future:

1. **Check compatibility** at https://developer.android.com/build/releases/gradle-plugin
2. **Upgrade path** (recommended order):
   - Gradle wrapper first
   - AGP second
   - Kotlin third (ensure KSP matches)
   - Other libraries last

3. **Major version upgrades** (Kotlin 2.0+):
   - Requires Gradle 8.5+
   - Requires AGP 8.5+
   - Can use Compose Compiler plugin instead of extension version
   - Update Hilt to 2.51+

## IDE Integration

### Android Studio
Recommended: **Android Studio Hedgehog (2023.1.1) or later**

The project should sync automatically. If issues occur:
1. File → Invalidate Caches → Invalidate and Restart
2. Delete `.idea` and `.gradle` folders
3. Reimport project

### IntelliJ IDEA
Requires: **IntelliJ IDEA 2023.2 or later** with Android plugin

## CI/CD Considerations

When setting up CI/CD:
- Use Java 17 or later
- Cache `.gradle` directory
- Use `--no-daemon` for CI builds
- Set `org.gradle.jvmargs=-Xmx2048m` in `gradle.properties`

## Project Status

✅ Build configuration is stable and tested
✅ All dependencies resolve correctly
✅ Ready for Phase 1.2 (Hilt Application setup)
