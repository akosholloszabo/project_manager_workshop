package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.runtime.Composable
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.NotesScreenContent as NewNotesScreenContent
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.ProjectsScreenContent as NewProjectsScreenContent
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.TicketsScreenContent as NewTicketsScreenContent

@Deprecated("Use hu.akosholloszabo.project_manager.project_manager_workshop.screens package instead")
fun ensureScreensMoved() {
}

// Compatibility wrappers - keep old top-level names working
@Composable
fun NotesScreenContent() {
    NewNotesScreenContent()
}

@Composable
fun ProjectsScreenContent() {
    NewProjectsScreenContent()
}

@Composable
fun TicketsScreenContent() {
    NewTicketsScreenContent()
}
