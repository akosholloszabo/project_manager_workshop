package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DetailEditorPane(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 8.dp,
    header: @Composable () -> Unit = {},
    isEditing: Boolean,
    editContent: @Composable () -> Unit,
    viewContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        header()

        if (isEditing) {
            editContent()
        } else {
            viewContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailEditorPanePreview() {
    PreviewWrapper(darkTheme = true) {
        DetailEditorPane(
            isEditing = false,
            header = { },
            editContent = { Text(text = "Editing") },
            viewContent = { Text(text = "Reading") }
        )
    }
}
