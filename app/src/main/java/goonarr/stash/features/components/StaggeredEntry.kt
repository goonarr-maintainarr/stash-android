package goonarr.stash.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A wrapper component that provides a staggered entry animation for its content.
 *
 * @param visible Whether the content should be visible and start the animation.
 * @param index The index of the item, used to calculate the animation delay.
 * @param modifier Optional [Modifier].
 * @param content The composable content to animate.
 */
@Composable
fun StaggeredEntry(
    modifier: Modifier = Modifier,
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    val enterDelay = index * 50 // 50ms stagger per item

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            // Slide up from 50px
            initialOffsetY = { 50 }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = enterDelay
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}
