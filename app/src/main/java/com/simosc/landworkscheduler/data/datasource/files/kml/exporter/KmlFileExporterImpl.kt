package com.simosc.landworkscheduler.data.datasource.files.kml.exporter

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPolygonPlacemark
import com.simosc.landworkscheduler.data.datasource.files.kml.typeconverters.toKmlPolygonPlacemark
import com.simosc.landworkscheduler.domain.extension.toArgbString
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.domain.model.Land

class KmlFileExporterImpl : KmlFileExporter {

    override fun generateKmlString(land: Land): String{
        land.toKmlPolygonPlacemark().let{ polygon ->
            return StringBuilder().apply{
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                append("  <Document>\n")
                if(polygon.outerBoundary.isNotEmpty()){
                    append(polygon.generateKmlStyleString())
                    append(polygon.generateKmlPlacemarkString())
                }
                append("  </Document>\n")
                append("</kml>")
            }.toString()
        }
    }

    override fun generateKmlString(lands: List<Land>): String{
        List(lands.size){
            lands[it].toKmlPolygonPlacemark()
        }.filter { it.outerBoundary.isNotEmpty() }.let{ polygons ->
            return StringBuilder().apply {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                append("  <Document>\n")
                polygons.forEach{ polygon ->
                    append(polygon.generateKmlStyleString())
                }
                polygons.forEach{ polygon ->
                    append(polygon.generateKmlPlacemarkString())
                }
                append("  </Document>\n")
                append("</kml>")
            }.toString()
        }
    }

    private fun KmlPolygonPlacemark.generateKmlStyleString(): String{
        return StringBuilder().apply{
            append("    <Style id=\"$id\">\n")
            append("        <LineStyle>\n")
            append("            <color>${color.copy(alpha = DefaultMapItemStrokeAlpha).toAbgrString()}</color>\n")
            append("            <width>$lineWidth</width>\n")
            append("        </LineStyle>\n")
            append("        <PolyStyle>\n")
            append("            <color>${color.copy(alpha = DefaultMapItemFillAlpha).toAbgrString()}</color>\n")
            append("        </PolyStyle>\n")
            append("    </Style>\n")
        }.toString()
    }

    private fun KmlPolygonPlacemark.generateKmlPlacemarkString(): String{
        return StringBuilder().apply{
            append("    <Placemark>\n")
            append("        <name>$name</name>\n")
            append("        <styleUrl>#$id</styleUrl>\n")
            append("        <Polygon>\n")
            append("            <tessellate>1</tessellate>\n")
            append("            <altitudeMode>clampToGround</altitudeMode>\n")
            append("            <outerBoundaryIs>\n")
            append("                <LinearRing>\n")
            append("                    <coordinates>\n")
            outerBoundary.forEach{
                append("                        ${it.toCoordinatesString()}\n")
            }
            if(outerBoundary.first() != outerBoundary.last())
                append("                        ${outerBoundary.first().toCoordinatesString()}\n")
            append("                    </coordinates>\n")
            append("                </LinearRing>\n")
            append("            </outerBoundaryIs>\n")
            innerBoundary.filter{it.isNotEmpty()}.let{ innerBoundary ->
                if(innerBoundary.isNotEmpty()){
                    append("            <innerBoundaryIs>\n")
                    innerBoundary.forEach{ coordinates ->
                        append("                <LinearRing>\n")
                        append("                    <coordinates>\n")
                        coordinates.forEach{
                            append("                        ${it.toCoordinatesString()}\n")
                        }
                        if(coordinates.first() != coordinates.last())
                            append("                        ${coordinates.first().toCoordinatesString()}\n")
                        append("                    </coordinates>\n")
                        append("                </LinearRing>\n")
                    }
                    append("            </innerBoundaryIs>\n")
                }
            }
            append("        </Polygon>\n")
            append("    </Placemark>\n")
        }.toString()
    }

    private fun LatLng.toCoordinatesString() =
        "$longitude,$latitude,0"

    private fun Color.toAbgrString(): String = toArgbString().let{
        "${it[0]}${it[1]}${it[6]}${it[7]}${it[4]}${it[5]}${it[2]}${it[3]}"
    }
}