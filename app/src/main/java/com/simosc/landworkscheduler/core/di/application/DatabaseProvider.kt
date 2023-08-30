package com.simosc.landworkscheduler.core.di.application

import android.content.Context
import androidx.room.Room
import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseProvider {

    @Provides
    fun provideLocalDatabase(@ApplicationContext applicationContext: Context): LocalDatabase =
        Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java,
            LocalDatabase.database_name
        ).build()

}