package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadOnlyDropdownField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    dropdownContent: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        val anchorModifier = modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
        TextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = anchorModifier,
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.exposedDropdownSize()
        ) {
            dropdownContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadOnlyDropdownFieldPreview() {
    PreviewWrapper(darkTheme = true) {
        ReadOnlyDropdownField(
            value = "Preview",
            label = "Label",
            expanded = true,
            onExpandedChange = {}
        ) {
            DropdownMenuItem(
                text = { Text("Item 1") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Item 2") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Item 3") },
                onClick = { }
            )
        }
    }
}
