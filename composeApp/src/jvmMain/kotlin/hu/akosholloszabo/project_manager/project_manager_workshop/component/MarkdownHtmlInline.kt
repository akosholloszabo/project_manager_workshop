package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Render a small, safe representation of inline HTML. We intentionally *do not* evaluate
 * or render arbitrary HTML — instead we display the raw HTML content in a neutral style.
 */
@Composable
fun MarkdownHtmlInline(html: String, modifier: Modifier = Modifier) {
    Text(
        text = AnnotatedString(html),
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownHtmlInlinePreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownHtmlInline("<kbd>Ctrl</kbd>+<kbd>C</kbd>")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownHtmlInlinePreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            MarkdownHtmlInline("<kbd>Ctrl</kbd>+<kbd>C</kbd>")
        }
    }
}
