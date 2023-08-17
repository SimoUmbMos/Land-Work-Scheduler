package com.simosc.landworkscheduler.data.datasource.files.kml.exporter

import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlLineString
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPlacemark
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPoint
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPolygon
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlStyle
import com.simosc.landworkscheduler.data.datasource.files.kml.typeconverters.getKmlRawEntities
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.domain.model.Land
import java.io.OutputStream

class KmlFileExporterImpl : KmlFileExporter {

    override suspend fun generateKml(
        lands: List<Land>,
        outputStream: OutputStream
    ): Boolean{
        lands.filter { it.border.isNotEmpty() }.let{ data ->
            if(data.isNotEmpty()) {
                StringBuilder().apply {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                    append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                    append("  <Document>\n")
                    val (styles, placemarks) = data.getKmlRawEntities()
                    styles.forEach { style ->
                        append(style.generateKmlStyleString())
                    }
                    placemarks.forEach { placemark ->
                        append(placemark.generateKmlPlacemarkString())
                    }
                    append("  </Document>\n")
                    append("</kml>")
                }.toString().let{ fileContent ->
                    outputStream.write(fileContent.toByteArray())
                    return true
                }
            }
        }
        return false
    }

    private fun KmlStyle.generateKmlStyleString(): String{
        return StringBuilder().apply{
            append("    <Style id=\"$id\">\n")
            if(lineColor != null || lineWidth != null) {
                append("        <LineStyle>\n")
                if(lineColor != null)
                    append("            <color>$lineColor</color>\n")
                if(lineWidth != null)
                    append("            <width>$lineWidth</width>\n")
                append("        </LineStyle>\n")
            }
            if(polyColor != null){
                append("        <PolyStyle>\n")
                append("            <color>$polyColor</color>\n")
                append("        </PolyStyle>\n")
            }
            append("    </Style>\n")
        }.toString()
    }

    private fun KmlPlacemark.generateKmlPlacemarkString(): String{
        return StringBuilder().apply{
            append("    <Placemark>\n")
            if(name != null)
                append("        <name>$name</name>\n")
            if(styleUrl != null)
                append("        <styleUrl>$styleUrl</styleUrl>\n")
            when(geometry){
                is KmlPolygon -> if(geometry.outerBoundary.isNotEmpty()){
                    append("        <Polygon>\n")
                    append("            <extrude>1</extrude>\n")
                    append("            <tessellate>1</tessellate>\n")
                    append("            <altitudeMode>clampToGround</altitudeMode>\n")
                    append("            <outerBoundaryIs>\n")
                    append("                <LinearRing>\n")
                    append("                    <coordinates>\n")
                    geometry.outerBoundary.forEach{
                        append("                        $it\n")
                    }
                    if(geometry.outerBoundary.first() != geometry.outerBoundary.last())
                        append("                        ${geometry.outerBoundary.first()}\n")
                    append("                    </coordinates>\n")
                    append("                </LinearRing>\n")
                    append("            </outerBoundaryIs>\n")
                    geometry.innerBoundary.filter{it.isNotEmpty()}.let{ innerBoundary ->
                        if(innerBoundary.isNotEmpty()){
                            append("            <innerBoundaryIs>\n")
                            innerBoundary.forEach{ coordinates ->
                                append("                <LinearRing>\n")
                                append("                    <coordinates>\n")
                                coordinates.forEach{
                                    append("                        $it\n")
                                }
                                if(coordinates.first() != coordinates.last())
                                    append("                        ${coordinates.first()}\n")
                                append("                    </coordinates>\n")
                                append("                </LinearRing>\n")
                            }
                            append("            </innerBoundaryIs>\n")
                        }
                    }
                    append("        </Polygon>\n")
                }
                is KmlLineString -> if(geometry.coordinates.isNotEmpty()){
                    append("        <LineString>\n")
                    append("            <extrude>1</extrude>\n")
                    append("            <tessellate>1</tessellate>\n")
                    append("            <altitudeMode>clampToGround</altitudeMode>\n")
                    append("            <coordinates>\n")
                    geometry.coordinates.forEach {
                        append("                $it\n")
                    }
                    append("            </coordinates>\n")
                    append("        </LineString>\n")
                }
                is KmlPoint -> {
                    append("        <Point>\n")
                    append("            <coordinates>${geometry.coordinates}</coordinates>\n")
                    append("        </Point>\n")
                }
                else -> {}
            }
            append("    </Placemark>\n")
        }.toString()
    }
}