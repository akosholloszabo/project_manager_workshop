package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProjectsScreenContent() {
    val projects = remember {
        listOf(
            Project(1, "Website Redesign", "Redesign corporate website."),
            Project(2, "Mobile App", "Initial MVP for mobile app."),
            Project(3, "Internal Tools", "Small tools to help operations.")
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Projects", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(projects.size) { project_index ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* placeholder */ }
                        .padding(8.dp)) {
                    Text(projects[project_index].name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(projects[project_index].description)
                    hu.akosholloszabo.project_manager.project_manager_workshop.SimpleDivider(
                        modifier = Modifier.padding(
                            top = 8.dp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent()
        }
    }
}
