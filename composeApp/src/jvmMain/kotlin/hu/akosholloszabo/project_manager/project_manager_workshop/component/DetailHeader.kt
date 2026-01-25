package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetailHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        // TODO If you get a modifier from outside, you should not override its width
        modifier = modifier.fillMaxWidth(),
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

// TODO Preview
