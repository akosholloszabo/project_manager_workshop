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
fun MarkdownLink(
    text: AnnotatedString,
    url: String,
    modifier: Modifier = Modifier,
    onClick: ((String) -> Unit)? = null
) {
    // This component is a convenience wrapper; clicking handling is performed by ClickableAnnotatedText in renderer.
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownLinkPreviewLight() {
    val ann = AnnotatedString("Compose website")
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownLink(text = ann, url = "https://www.jetbrains.com", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownLinkPreviewDark() {
    val ann = AnnotatedString("Compose website")
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownLink(text = ann, url = "https://www.jetbrains.com", modifier = Modifier.padding(16.dp))
        }
    }
}
