package com.simosc.landworkscheduler.core.di.viewmodel

import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import com.simosc.landworkscheduler.data.repository.LocalLandRepositoryImpl
import com.simosc.landworkscheduler.data.repository.LocalNoteRepositoryImpl
import com.simosc.landworkscheduler.data.repository.LocalWorkRepositoryImpl
import com.simosc.landworkscheduler.data.repository.LocalZoneRepositoryImpl
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object LocalRepositoriesProvider {

    @Provides
    fun provideLocalLandRepository(db: LocalDatabase): LocalLandRepository =
        LocalLandRepositoryImpl(db)

    @Provides
    fun provideLocalZoneRepositoryImp(db: LocalDatabase): LocalZoneRepository =
        LocalZoneRepositoryImpl(db)

    @Provides
    fun provideLocalNoteRepository(db: LocalDatabase): LocalNoteRepository =
        LocalNoteRepositoryImpl(db)

    @Provides
    fun provideLocalScheduledWorkRepository(db: LocalDatabase): LocalWorkRepository =
        LocalWorkRepositoryImpl(db)

}