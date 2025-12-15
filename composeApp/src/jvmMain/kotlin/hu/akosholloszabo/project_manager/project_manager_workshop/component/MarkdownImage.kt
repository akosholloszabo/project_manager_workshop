package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Box
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
fun MarkdownImage(src: String, alt: String?, modifier: Modifier = Modifier) {
    // For previews and simplicity we show alt text; real implementation may use Coil/AsyncImage.
    Box(modifier = modifier.padding(8.dp)) {
        Text(text = alt ?: src, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun MarkdownImagePreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownImage(
                src = "https://example.com/image.png",
                alt = "Example image",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun MarkdownImagePreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownImage(
                src = "https://example.com/image.png",
                alt = "Example image",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

