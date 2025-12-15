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
fun MarkdownOrderedList(
    items: List<Pair<Boolean?, AnnotatedString>>,
    startIndex: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var idx = startIndex
        for (item in items) {
            val checked = item.first
            val ann = item.second
            if (checked != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("${idx}. ")
                    Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(ann)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("${idx}. ")
                    Text(ann)
                }
            }
            idx++
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownOrderedListPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarkdownOrderedList(listOf(null to AnnotatedString("First"), true to AnnotatedString("Done")))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownOrderedListPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color(0xFF121212)) {
            MarkdownOrderedList(listOf(null to AnnotatedString("First"), true to AnnotatedString("Done")))
        }
    }
}
