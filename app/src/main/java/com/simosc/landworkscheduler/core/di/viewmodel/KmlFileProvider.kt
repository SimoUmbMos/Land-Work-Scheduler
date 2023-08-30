package com.simosc.landworkscheduler.core.di.viewmodel

import com.simosc.landworkscheduler.data.datasource.files.kml.exporter.KmlFileExporterImpl
import com.simosc.landworkscheduler.data.datasource.files.kml.importer.KmlFileImporterImpl
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.domain.files.KmlFileImporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import org.xmlpull.v1.XmlPullParserFactory

@Module
@InstallIn(ViewModelComponent::class)
object KmlFileProvider {

    @Provides
    fun provideKmlFileExporter(): KmlFileExporter =
        KmlFileExporterImpl()

    @Provides
    fun provideKmlFileImporter(): KmlFileImporter = KmlFileImporterImpl(
        xmlPullParserFactory = XmlPullParserFactory.newInstance()
    )
}