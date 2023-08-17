package com.simosc.landworkscheduler

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simosc.landworkscheduler.data.datasource.files.kml.importer.KmlFileImporterImpl
import com.simosc.landworkscheduler.domain.usecase.file.ReadKml
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserFactory


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class KmlImporterTest {
    private val tag = "KmlImporterTest"
    private val xmlPullParserFactory = XmlPullParserFactory.newInstance()

    @Test
    fun kmlImporter_readSimpleKml() {
        val inputSteam = javaClass.getResourceAsStream("kml_file_1.kml")
        Assert.assertTrue(inputSteam != null)

        val readKml = ReadKml(
            KmlFileImporterImpl(
                debugPrint = { kmlStyles, kmlPlacemarks ->
                    kmlStyles.forEach {
                        Log.d(tag,
                            "kmlStyle id=${it.id} polyColor=${it.polyColor} lineColor=${it.lineColor} lineWidth=${it.lineWidth}"
                        )
                    }
                    kmlPlacemarks.forEach{
                        Log.d(tag,
                            "kmlPlacemark name=${it.name} styleUrl=${it.styleUrl} description=${it.description} geometry=${it.geometry}"
                        )
                    }
                },
                xmlPullParserFactory = xmlPullParserFactory
            )
        )
        runTest {
            inputSteam?.use {
                readKml(it).let { lands ->
                    lands.forEach{ land ->
                        Log.d(tag,
                            "Land id=${land.id} title=${land.title} color=${land.color} border=${land.border} holes=${land.holes}"
                        )
                    }
                }
            }
        }
    }
}