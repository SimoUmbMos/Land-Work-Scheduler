package com.simosc.landworkscheduler

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.data.datasource.files.kml.exporter.KmlFileExporterImpl
import com.simosc.landworkscheduler.domain.extension.parseColor
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.GenerateKml
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayOutputStream

class KmlUnitTest {
    private val generateKml = GenerateKml(KmlFileExporterImpl())
    private fun getLandsMockList(
        size: Int,
        generateBorder: Boolean = true,
        generateHole: Boolean = true,
        generateMultipleHoles: Boolean = false
    ) = List(size){ i ->
        Land(
            id = i + 1L,
            title = "Mock Land ${i + 1}",
            color = Color.parseColor("ffffffff"),
            border = if(generateBorder) listOf(
                LatLng(0.0,0.0),
                LatLng(1.0,0.0),
                LatLng(1.0,1.0),
                LatLng(0.0,0.1)
            )  else emptyList(),
            holes = if(generateHole){
                if(generateMultipleHoles){
                    listOf(
                        listOf(
                            LatLng(0.25,0.25),
                            LatLng(0.5,0.25),
                            LatLng(0.5,0.5),
                            LatLng(0.25,0.5),
                        ),
                        listOf(
                            LatLng(0.5,0.5),
                            LatLng(0.75,0.5),
                            LatLng(0.75,0.75),
                            LatLng(0.5,0.75),
                        ),
                    )
                }else{
                    listOf(
                        listOf(
                            LatLng(0.25,0.25),
                            LatLng(0.75,0.25),
                            LatLng(0.75,0.75),
                            LatLng(0.25,0.75),
                        )
                    )
                }
            }else{
                emptyList()
            }
        )
    }

    private fun String.count(other: String): Int =
        this.split(other).size - 1

    @Test
    fun kmlExportLands_emptyList() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val result = generateKml(
                emptyList(),
                outputSteam
            )

            println("")
            println("kmlExportLands_emptyList:")
            println(outputSteam)
            println("")

            assertFalse(result)
            assertTrue(outputSteam.toString().isEmpty())
        }
    }

    @Test
    fun kmlExportLands_SingleItem() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val size = 1
            val result = generateKml(
                getLandsMockList(size),
                outputSteam
            )

            println("")
            println("kmlExportLands_SingleItem:")
            println(outputSteam)
            println("")

            assertTrue(result)
            outputSteam.toString().let{ kmlContent ->
                assertTrue(kmlContent.isNotBlank())
                assertEquals(size, kmlContent.count("<Style"))
                assertEquals(size, kmlContent.count("<Placemark>"))
                assertEquals(size, kmlContent.count("<Polygon>"))
                assertEquals(size, kmlContent.count("<outerBoundaryIs>"))
                assertEquals(size, kmlContent.count("<innerBoundaryIs>"))
            }
        }
    }

    @Test
    fun kmlExportLands_MultipleItems() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val size = 10
            val result = generateKml(
                getLandsMockList(size),
                outputSteam
            )

            println("")
            println("kmlExportLands_MultipleItems:")
            println(outputSteam)
            println("")

            assertTrue(result)
            outputSteam.toString().let{ kmlContent ->
                assertTrue(kmlContent.isNotBlank())
                assertEquals(size, kmlContent.count("<Style"))
                assertEquals(size, kmlContent.count("<Placemark>"))
                assertEquals(size, kmlContent.count("<Polygon>"))
                assertEquals(size, kmlContent.count("<outerBoundaryIs>"))
                assertEquals(size, kmlContent.count("<innerBoundaryIs>"))
            }
        }
    }

    @Test
    fun kmlExportLands_MultipleHolesItems() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val size = 10
            val result = generateKml(
                getLandsMockList(size, generateMultipleHoles = true),
                outputSteam
            )

            println("")
            println("kmlExportLands_MultipleHolesItems:")
            println(outputSteam)
            println("")

            assertTrue(result)
            outputSteam.toString().let{ kmlContent ->
                assertTrue(kmlContent.isNotBlank())
                assertEquals(size, kmlContent.count("<Style"))
                assertEquals(size, kmlContent.count("<Placemark>"))
                assertEquals(size, kmlContent.count("<Polygon>"))
                assertEquals(size, kmlContent.count("<outerBoundaryIs>"))
                assertEquals(size, kmlContent.count("<innerBoundaryIs>"))
                assertEquals(size, kmlContent.replace("\\s+".toRegex(),"").count("</LinearRing><LinearRing>"))
            }
        }
    }

    @Test
    fun kmlExportLands_NoBordersLands() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val size = 10
            val result = generateKml(
                getLandsMockList(size, generateBorder = false),
                outputSteam
            )

            println("")
            println("kmlExportLands_NoBordersLands:")
            println(outputSteam)
            println("")

            assertFalse(result)
            assertTrue(outputSteam.toString().isEmpty())
        }
    }

    @Test
    fun kmlExportLands_NoHoles() = runTest {
        ByteArrayOutputStream().use { outputSteam ->
            val size = 10
            val result = generateKml(
                getLandsMockList(size, generateHole = false),
                outputSteam
            )

            println("")
            println("kmlExportLands_MultipleItems:")
            println(outputSteam)
            println("")

            assertTrue(result)
            outputSteam.toString().let{ kmlContent ->
                assertTrue(kmlContent.isNotBlank())
                assertEquals(size, kmlContent.count("<Style"))
                assertEquals(size, kmlContent.count("<Placemark>"))
                assertEquals(size, kmlContent.count("<Polygon>"))
                assertEquals(size, kmlContent.count("<outerBoundaryIs>"))
                assertEquals(0, kmlContent.count("<innerBoundaryIs>"))
            }
        }
    }

}