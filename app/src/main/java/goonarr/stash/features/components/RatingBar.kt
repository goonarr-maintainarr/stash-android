package goonarr.stash.features.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp,
    // Gold
    activeColor: Color = Color(0xFFFFD700),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
) {
    val haptic = LocalStashHapticFeedback.current
    Row(modifier = modifier) {
        for (i in 1..5) {
            RatingStar(
                index = i,
                rating = rating,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                starSize = starSize,
                onClick = if (onRatingChanged != null) {
                    {
                        haptic.perform(StashHapticFeedbackType.Selection)
                        if (rating == i) {
                            onRatingChanged(0)
                        } else {
                            onRatingChanged(i)
                        }
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun RatingStar(
    index: Int,
    rating: Int,
    activeColor: Color,
    inactiveColor: Color,
    starSize: Dp,
    onClick: (() -> Unit)?
) {
    var isBouncing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isBouncing) 1.25f else 1f,
        animationSpec = spring(
            // MediumBouncy is 0.5, slightly lower for more bounce
            dampingRatio = 0.4f,
            stiffness = Spring.StiffnessLow
        ),
        label = "StarBounce",
        finishedListener = { if (isBouncing) isBouncing = false }
    )

    var modifier = Modifier
        .size(starSize)
        .scale(scale)
        .padding(2.dp)

    if (onClick != null) {
        modifier = modifier.clickable {
            isBouncing = true
            onClick()
        }
    }

    Icon(
        imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = "Star $index",
        tint = if (index <= rating) activeColor else inactiveColor,
        modifier = modifier
    )
}
