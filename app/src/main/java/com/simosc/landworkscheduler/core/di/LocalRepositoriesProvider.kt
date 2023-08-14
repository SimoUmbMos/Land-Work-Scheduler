package com.simosc.landworkscheduler.core.di

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
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocalRepositoriesProvider {

    @Provides
    fun provideLocalLandRepository(
        db: LocalDatabase
    ): LocalLandRepository = LocalLandRepositoryImpl(
        landDao = db.localLandDao(),
        zoneDao = db.localZoneDao(),
        noteDao = db.localNoteDao(),
        workDao = db.localWorkDao()
    )

    @Provides
    fun provideLocalZoneRepositoryImp(
        db: LocalDatabase
    ): LocalZoneRepository = LocalZoneRepositoryImpl(
        zoneDao = db.localZoneDao(),
        workDao = db.localWorkDao()
    )

    @Provides
    fun provideLocalNoteRepository(
        db: LocalDatabase
    ): LocalNoteRepository = LocalNoteRepositoryImpl(
        noteDao = db.localNoteDao()
    )

    @Provides
    fun provideLocalScheduledWorkRepository(
        db: LocalDatabase
    ): LocalWorkRepository = LocalWorkRepositoryImpl(
        workDao = db.localWorkDao()
    )

}