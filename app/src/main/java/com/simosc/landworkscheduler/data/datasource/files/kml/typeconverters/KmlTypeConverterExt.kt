package com.simosc.landworkscheduler.data.datasource.files.kml.typeconverters

import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPolygonPlacemark
import com.simosc.landworkscheduler.domain.model.Land

fun KmlPolygonPlacemark.toLand() = Land(
    id = id,
    title = name,
    color = color,
    border = outerBoundary.toList(),
    holes = innerBoundary.filter { it.isNotEmpty() }.toList()
)

fun Land.toKmlPolygonPlacemark() = KmlPolygonPlacemark(
    id = id,
    name = title,
    color = color,
    outerBoundary = border.toList(),
    innerBoundary = holes.filter { it.isNotEmpty() }.toList()
)