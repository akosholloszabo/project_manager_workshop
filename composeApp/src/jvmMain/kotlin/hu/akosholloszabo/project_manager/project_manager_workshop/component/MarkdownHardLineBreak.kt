package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MarkdownHardLineBreak(modifier: Modifier = Modifier) {
    // Represent a hard line break as an explicit new line in text; callers should append this where needed.
    Text("\n", style = MaterialTheme.typography.bodyLarge, modifier = modifier)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownHardLineBreakPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownHardLineBreak()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownHardLineBreakPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownHardLineBreak()
        }
    }
}
