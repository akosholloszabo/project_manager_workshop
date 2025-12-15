package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Render a soft line break: usually treated as a single space when joining lines. */
@Composable
fun MarkdownSoftLineBreak(modifier: Modifier = Modifier) {
    Text(" ", style = MaterialTheme.typography.bodyLarge, modifier = modifier)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownSoftLineBreakPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownSoftLineBreak()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownSoftLineBreakPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownSoftLineBreak()
        }
    }
}
