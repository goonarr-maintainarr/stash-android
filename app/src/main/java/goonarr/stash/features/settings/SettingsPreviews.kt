package goonarr.stash.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.core.network.WebSocketState
import goonarr.stash.util.MockData

@Preview(showBackground = true, name = "All Collapsed")
@Composable
fun SettingsScreenPreview_AllCollapsed() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://192.168.1.100:9999/graphql",
            apiKey = "abc123",
            testSuccess = true,
            scanOptionsExpanded = false,
            generationOptionsExpanded = false,
            jobs = MockData.jobs,
            webSocketState = WebSocketState.Connected
        )
    }
}

@Preview(showBackground = true, name = "Scan Expanded")
@Composable
fun SettingsScreenPreview_ScanExpanded() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://192.168.1.100:9999/graphql",
            apiKey = "abc123",
            testSuccess = true,
            scanOptionsExpanded = true,
            generationOptionsExpanded = false,
            jobs = emptyList(),
            webSocketState = WebSocketState.Connected
        )
    }
}

@Preview(showBackground = true, name = "Generation Expanded")
@Composable
fun SettingsScreenPreview_GenerationExpanded() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://192.168.1.100:9999/graphql",
            apiKey = "abc123",
            testSuccess = true,
            scanOptionsExpanded = false,
            generationOptionsExpanded = true,
            jobs = emptyList(),
            webSocketState = WebSocketState.Connected
        )
    }
}

@Preview(showBackground = true, name = "All Expanded")
@Composable
fun SettingsScreenPreview_AllExpanded() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://192.168.1.100:9999/graphql",
            apiKey = "abc123",
            scanOptionsExpanded = true,
            generationOptionsExpanded = true,
            jobs = MockData.jobs,
            webSocketState = WebSocketState.Connected
        )
    }
}

@Preview(showBackground = true, name = "Connection Testing")
@Composable
fun SettingsScreenPreview_Testing() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://192.168.1.100:9999/graphql",
            apiKey = "secret-key",
            isTesting = true,
            jobs = listOf(MockData.scanningJob),
            webSocketState = WebSocketState.Connecting
        )
    }
}

@Preview(showBackground = true, name = "Connection Error")
@Composable
fun SettingsScreenPreview_Error() {
    StashTheme {
        SettingsScreenContent(
            serverUrl = "http://invalid-url",
            apiKey = "",
            testMessage = "Connection failed: Unable to reach server",
            testSuccess = false,
            jobs = emptyList(),
            webSocketState = WebSocketState.Error("Connection refused")
        )
    }
}
