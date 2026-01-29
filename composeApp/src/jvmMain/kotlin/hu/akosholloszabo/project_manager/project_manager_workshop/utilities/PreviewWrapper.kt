package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme


@Composable
fun PreviewWrapper(
    darkTheme: Boolean ,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
