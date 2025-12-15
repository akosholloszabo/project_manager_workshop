package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Renders a single list item. If `checked` is not null, show a checkbox (task list). If `index` is
 * provided, show numbered prefix.
 */
@Composable
fun MarkdownListItem(
    index: Int? = null,
    checked: Boolean? = null,
    label: androidx.compose.ui.text.AnnotatedString,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    val state = remember { mutableStateOf(checked ?: false) }
    Row(modifier = modifier.padding(vertical = 4.dp)) {
        if (checked != null) {
            Checkbox(checked = state.value, onCheckedChange = {
                state.value = it
                onCheckedChange?.invoke(it)
            })
            Text(text = " ")
        } else if (index != null) {
            Text(text = "${index}.", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text(text = "•", style = MaterialTheme.typography.bodyLarge)
        }
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}

