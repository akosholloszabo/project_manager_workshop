package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getTextOrException
import org.koin.compose.getKoin

@Composable
fun CrudActionBar(
    hasSelection: Boolean,
    isEditing: Boolean,
    onNew: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    labels: CrudActionLabels = CrudActionLabels(
        newLabel = getKoin().getTextOrException("crud.new"),
        editLabel = getKoin().getTextOrException("crud.edit"),
        saveLabel = getKoin().getTextOrException("crud.save"),
        deleteLabel = getKoin().getTextOrException("crud.delete")
    ),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        onNew?.let { Button(onClick = it) { Text(labels.newLabel) } }
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

