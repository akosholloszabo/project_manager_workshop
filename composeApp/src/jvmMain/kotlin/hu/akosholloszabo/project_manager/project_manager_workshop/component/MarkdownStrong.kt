package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MarkdownStrong(text: AnnotatedString, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge.copy(), modifier = modifier.padding(0.dp))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownStrongPreviewLight() {
    val ann = AnnotatedString("Bold text example")
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownStrong(text = ann, modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownStrongPreviewDark() {
    val ann = AnnotatedString("Bold text example")
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownStrong(text = ann, modifier = Modifier.padding(16.dp))
        }
    }
}

