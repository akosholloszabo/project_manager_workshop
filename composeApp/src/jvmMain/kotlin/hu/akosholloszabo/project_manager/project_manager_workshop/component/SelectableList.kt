package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> SelectableList(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedKey: String?,
    keyOf: (T) -> String,
    onItemClick: (T) -> Unit,
    itemContent: @Composable (T, Boolean) -> Unit
) {
    //TODO Integer goes to resources
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // TODO If one parameter has named argument, all should have
        items(items, key = { keyOf(it) }) { entry ->
            val isSelected = keyOf(entry) == selectedKey
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected)
                            // TODO Integer goes to resources
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else
                            Color.Transparent
                    )
                    .clickable { onItemClick(entry) }
                    // TODO Integer goes to resources
                    .padding(8.dp)
            ) {
                itemContent(entry, isSelected)
            }
        }
    }
}

// TODO Preview
