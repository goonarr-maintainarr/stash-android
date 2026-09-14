package goonarr.stash.features.performers

import kotlin.math.roundToInt

fun formatHeight(heightCm: Int): String {
    val totalInches = (heightCm / 2.54).roundToInt()
    val feet = totalInches / 12
    val inches = totalInches % 12
    return "$feet'$inches\" (${heightCm}cm)"
}

fun formatWeight(weightKg: Int): String {
    val lbs = (weightKg * 2.20462).roundToInt()
    return "$lbs lbs (${weightKg}kg)"
}
