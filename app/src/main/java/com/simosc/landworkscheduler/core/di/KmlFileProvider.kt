package com.simosc.landworkscheduler.core.di

import com.simosc.landworkscheduler.data.datasource.files.kml.exporter.KmlFileExporterImpl
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object KmlFileProvider {

    @Provides
    fun provideKmlFileExporter(): KmlFileExporter =
        KmlFileExporterImpl()
}