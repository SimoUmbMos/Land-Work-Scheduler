package com.simosc.landworkscheduler.data.datasource.files.kml.importer

import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlLineString
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPlacemark
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPoint
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPolygon
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlStyle
import com.simosc.landworkscheduler.data.datasource.files.kml.typeconverters.getAllLands
import com.simosc.landworkscheduler.domain.extension.trimWithSingleWhitespaces
import com.simosc.landworkscheduler.domain.files.KmlFileImporter
import com.simosc.landworkscheduler.domain.model.Land
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class KmlFileImporterImpl(
    private val debugPrint: (List<KmlStyle>, List<KmlPlacemark>) -> Unit = {_,_ ->},
    private val xmlPullParserFactory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
): KmlFileImporter {

    override suspend fun readLandsFromKml(
        inputStream: InputStream,
    ): List<Land> {
        val styles = mutableListOf<KmlStyle>()
        val placemarks = mutableListOf<KmlPlacemark>()
        val parser: XmlPullParser = xmlPullParserFactory.apply {
            isNamespaceAware = true
        }.newPullParser()
        try{
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG){
                    parser.name.let{ tag ->
                        when{
                            tag.equals("Style", true) ->
                                readKmlStyle(parser)?.let{
                                    styles.add(it)
                                }

                            tag.equals("Placemark", true) ->
                                readKmlPlacemark(parser)?.let{
                                    placemarks.add(it)
                                }

                            else -> {}
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception){
            throw e
        }

        debugPrint(styles.toList(),placemarks.toList())
        return getAllLands(
            styles.toList(),
            placemarks.toList()
        )
    }

    private fun readKmlStyle(
        parser: XmlPullParser
    ): KmlStyle?{

        var eventType = parser.eventType

        val tags: MutableList<String> = mutableListOf()

        val id = parser.getAttributeValue(null,"id")
        var polyColor: String? = null
        var lineColor: String? = null
        var lineWidth: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT){
            when(eventType){

                XmlPullParser.START_TAG -> {
                    tags.add(parser.name)
                }

                XmlPullParser.TEXT -> {
                    val currTag: String?
                    val previousTag: String?

                    if(tags.size > 1){
                        currTag = tags.last()
                        previousTag = tags[tags.size - 2]
                    }else if(tags.size == 1){
                        currTag = tags.last()
                        previousTag = null
                    }else{
                        currTag = null
                        previousTag = null
                    }

                    if(previousTag != null && previousTag.equals("LineStyle",true)){
                        when{

                            currTag.equals("color",true) ->
                                lineColor = parser.text

                            currTag.equals("width",true) ->
                                lineWidth = parser.text

                        }
                    }else if(previousTag != null && previousTag.equals("PolyStyle",true)){
                        if(currTag.equals("color",true)){
                            polyColor = parser.text
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if(tags.size > 0){
                        tags.removeAt(tags.size - 1)
                    }
                }

            }

            if(tags.isEmpty()){
                return KmlStyle(
                    id = id,
                    lineColor = lineColor,
                    lineWidth = lineWidth?.toDoubleOrNull() ?: 1.0,
                    polyColor = polyColor,
                )
            }else{
                eventType = parser.next()
            }
        }
        return null
    }

    private fun readKmlPlacemark(
        parser: XmlPullParser
    ): KmlPlacemark?{
        var eventType = parser.eventType
        val tags: MutableList<String> = mutableListOf()

        var name: String? = null
        var description: String? = null
        var styleUrl: String? = null
        var polygon: KmlPolygon? = null
        var lineString: KmlLineString? = null
        var point: KmlPoint? = null

        while (eventType != XmlPullParser.END_DOCUMENT){

            when(eventType){
                XmlPullParser.START_TAG -> {
                    tags.add(parser.name)
                }

                XmlPullParser.TEXT -> {
                    tags.lastOrNull()?.let{ currTag ->
                        when{
                            currTag.equals("name",true) -> {
                                name = parser.text.trimWithSingleWhitespaces()
                            }

                            currTag.equals("styleUrl",true) -> {
                                styleUrl = parser.text.trimWithSingleWhitespaces()
                            }

                            currTag.equals("description",true) -> {
                                description = parser.text.trimWithSingleWhitespaces()
                            }

                            currTag.equals("coordinates", true) -> {
                                when{
                                    tags.any { it.equals("Polygon",true) } -> {
                                        if(tags.any{ it.equals("outerBoundaryIs",true) }){
                                            polygon?.let{
                                                polygon = it.copy(
                                                    outerBoundary = parser.text.trimWithSingleWhitespaces().split(" ")
                                                )
                                            }?:run{
                                                polygon = KmlPolygon(
                                                    outerBoundary = parser.text.trimWithSingleWhitespaces().split(" "),
                                                    innerBoundary = emptyList()
                                                )
                                            }
                                        }else if(tags.any{ it.equals("innerBoundaryIs",true) }){
                                            polygon?.let{
                                                polygon = it.copy(
                                                    innerBoundary = it.innerBoundary.toMutableList().apply {
                                                        add(parser.text.trimWithSingleWhitespaces().split(" "))
                                                    }.toList()
                                                )
                                            }?:run{
                                                polygon = KmlPolygon(
                                                    outerBoundary = emptyList(),
                                                    innerBoundary = listOf(
                                                        parser.text.trimWithSingleWhitespaces().split(" ")
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    tags.any { it.equals("LineString",true) } -> {
                                        lineString = KmlLineString(
                                            coordinates = parser.text.trimWithSingleWhitespaces().split(" ")
                                        )
                                    }

                                    tags.any { it.equals("Point",true) } -> {
                                        point = KmlPoint(
                                            coordinates = parser.text
                                        )
                                    }
                                }

                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if(tags.size > 0){
                        tags.removeAt(tags.size - 1)
                    }
                }
            }

            if(tags.isEmpty()){
                return KmlPlacemark(
                    name = name,
                    styleUrl = styleUrl,
                    description = description,
                    geometry = when{
                        polygon != null && polygon?.outerBoundary?.isNotEmpty()?:false ->
                            polygon

                        lineString != null && lineString?.coordinates?.isNotEmpty()?:false ->
                            lineString

                        point != null ->
                            point

                        else ->
                            null
                    }
                )
            }else{
                eventType = parser.next()
            }
        }
        return null
    }
}