package com.simosc.landworkscheduler

import com.simosc.landworkscheduler.data.datasource.files.kml.exporter.KmlFileExporterImpl
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.GenerateKml
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class KmlUnitTest {
    private val generateKml = GenerateKml(KmlFileExporterImpl())

    @Test
    fun kmlExportLands_emptyList() = runTest {
        val outputStream: OutputStream = ByteArrayOutputStream()
        val lands: List<Land> = emptyList()
        outputStream.use {
            generateKml(lands,outputStream)
        }
    }

    @Test
    fun kmlExportLands_SingleItem() = runTest {
        val outputStream: OutputStream = ByteArrayOutputStream()
        val lands: List<Land> = emptyList() //todo
        outputStream.use {
            generateKml(lands,outputStream)
        }
    }

    @Test
    fun kmlExportLands_MultipleItems() = runTest {
        val outputStream: OutputStream = ByteArrayOutputStream()
        val lands: List<Land> = emptyList() //todo
        outputStream.use {
            generateKml(lands,outputStream)
        }
    }

    @Test
    fun kmlExportLands_NoBordersLands() = runTest {
        val outputStream: OutputStream = ByteArrayOutputStream()
        val lands: List<Land> = emptyList() //todo
        outputStream.use {
            generateKml(lands,outputStream)
        }
    }

}