package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DetailHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
        actions()
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailHeaderPreview() {
    PreviewWrapper(darkTheme = true) {
        DetailHeader(title = "Title", subtitle = "Subtitle")
    }
}
