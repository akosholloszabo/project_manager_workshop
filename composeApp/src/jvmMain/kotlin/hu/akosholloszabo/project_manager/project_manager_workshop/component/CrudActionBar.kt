package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_delete
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_edit
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_new
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_save
import org.jetbrains.compose.resources.stringResource

@Composable
fun CrudActionBar(
    modifier: Modifier = Modifier,
    hasSelection: Boolean,
    isEditing: Boolean,
    onNew: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    labels: CrudActionLabels = CrudActionLabels(
        newLabel = stringResource(Res.string.crud_new),
        editLabel = stringResource(Res.string.crud_edit),
        saveLabel = stringResource(Res.string.crud_save),
        deleteLabel = stringResource(Res.string.crud_delete)
    )
) {
    Row(
        modifier = modifier,
        // TODO Integer goes to resources
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        onNew?.let { Button(onClick = it) { Text(labels.newLabel) } }
        // TODO Write it with takeIf
        if (hasSelection) {
            if (isEditing) {
                onSave?.let { Button(onClick = it) { Text(labels.saveLabel) } }
            } else {
                onEdit?.let { Button(onClick = it) { Text(labels.editLabel) } }
            }
            onDelete?.let { Button(onClick = it) { Text(labels.deleteLabel) } }
        }
    }
}

//TODO Preview
