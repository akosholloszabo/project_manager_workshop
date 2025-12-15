package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("UNUSED_PARAMETER")
@Composable
fun MarkdownBulletList(items: List<Pair<Boolean?, AnnotatedString>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        for (item in items) {
            val checked = item.first
            val ann = item.second
            if (checked != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(ann)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("• ")
                    Text(ann)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownBulletListPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarkdownBulletList(listOf(null to AnnotatedString("Item 1"), true to AnnotatedString("Done")))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownBulletListPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color(0xFF121212)) {
            MarkdownBulletList(listOf(null to AnnotatedString("Item 1"), true to AnnotatedString("Done")))
        }
    }
}
