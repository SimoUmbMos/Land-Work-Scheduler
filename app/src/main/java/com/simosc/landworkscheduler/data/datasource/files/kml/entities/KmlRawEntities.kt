package com.simosc.landworkscheduler.data.datasource.files.kml.entities

data class KmlPlacemark(
    val name: String?,
    val description: String?,
    val styleUrl: String?,
    val geometry: KmlGeometry?
)

data class KmlStyle(
    val id: String,
    val lineColor :String?,
    val lineWidth :Double?,
    val polyColor : String?
)

sealed class KmlGeometry

data class KmlPoint(
    val coordinates: String
): KmlGeometry()

data class KmlLineString(
    val coordinates: List<String>
): KmlGeometry()

data class KmlPolygon(
    val outerBoundary: List<String>,
    val innerBoundary: List<List<String>>,
):KmlGeometry()