package goonarr.stash.features.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

fun Modifier.bounceClickable(
    minScale: Float = 0.96f,
    minElevation: Dp = 2.dp,
    maxElevation: Dp = 6.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    spotColor: Color = Color.Black,
    ambientColor: Color = Color.Black,
    onClick: () -> Unit
) = composed {
    val haptic = LocalStashHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) minScale else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val currentShadowElevation by animateDpAsState(
        targetValue = if (isPressed) minElevation else maxElevation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "shadowElevation"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = currentShadowElevation,
            shape = shape,
            clip = true,
            spotColor = spotColor,
            ambientColor = ambientColor
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            haptic.perform(StashHapticFeedbackType.Selection)
            onClick()
        }
}
