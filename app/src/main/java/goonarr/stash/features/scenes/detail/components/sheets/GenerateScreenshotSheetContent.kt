package goonarr.stash.features.scenes.detail.components.sheets

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * Modal sheet content for confirming screenshot generation.
 *
 * @param previewBitmap The captured frame bitmap to show as a preview.
 * @param onGenerate Callback when the user confirms screenshot generation.
 * @param onCancel Callback when the user cancels the action.
 */
@Composable
fun GenerateScreenshotSheetContent(
    previewBitmap: Bitmap?,
    onGenerate: () -> Unit,
    onCancel: () -> Unit
) {
    val dynamicColors = LocalStashDynamicColors.current
    val triadicGradient = remember(dynamicColors) {
        Brush.sweepGradient(
            listOf(
                dynamicColors.primary,
                dynamicColors.tertiary,
                dynamicColors.quaternary,
                dynamicColors.primary
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (previewBitmap != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, triadicGradient)
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Screenshot Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = "Generate Screenshot",
            style = MaterialTheme.typography.titleLarge,
            color = dynamicColors.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Replace the current scene screenshot with one from the current playback time?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        val haptic = LocalStashHapticFeedback.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Light)
                    onCancel()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onGenerate()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = dynamicColors.quaternary,
                contentColor = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Generate at Current Time",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
