package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MarkdownThematicBreak(modifier: Modifier = Modifier) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = modifier.padding(vertical = 8.dp))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownThematicBreakPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            MarkdownThematicBreak(modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
fun MarkdownThematicBreakPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = androidx.compose.ui.graphics.Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            MarkdownThematicBreak(modifier = Modifier.padding(16.dp))
        }
    }
}
