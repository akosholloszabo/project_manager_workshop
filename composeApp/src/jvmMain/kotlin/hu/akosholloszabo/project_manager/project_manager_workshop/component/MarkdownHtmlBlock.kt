package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MarkdownHtmlBlock(html: String, modifier: Modifier = Modifier) {
    // For safety we render HTML as plain text in the viewer.
    Text(text = html, style = MaterialTheme.typography.bodyLarge, modifier = modifier.padding(8.dp))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 160)
@Composable
fun MarkdownHtmlBlockPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownHtmlBlock(html = "<iframe src=...></iframe>", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 160)
@Composable
fun MarkdownHtmlBlockPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownHtmlBlock(html = "<iframe src=...></iframe>", modifier = Modifier.padding(16.dp))
        }
    }
}

