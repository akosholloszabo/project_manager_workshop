package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val localModule = module {
    singleOf(::FileStorageHelper)
    singleOf(::WorkingFolderViewModel)
}
