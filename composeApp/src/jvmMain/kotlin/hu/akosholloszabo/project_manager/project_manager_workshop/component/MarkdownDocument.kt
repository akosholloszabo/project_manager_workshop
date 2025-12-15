package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Simple container for a parsed Document. The renderer currently iterates the document
 * and emits children directly; this component is provided for completeness and previews.
 */
@Composable
fun MarkdownDocument(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownDocumentPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownDocument { /* small example */ }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MarkdownDocumentPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownDocument { /* small example */ }
        }
    }
}

