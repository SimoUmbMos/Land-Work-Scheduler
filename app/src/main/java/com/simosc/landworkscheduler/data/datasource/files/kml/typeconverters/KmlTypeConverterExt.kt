package com.simosc.landworkscheduler.data.datasource.files.kml.typeconverters

import androidx.compose.ui.graphics.Color
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPlacemark
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlPolygon
import com.simosc.landworkscheduler.data.datasource.files.kml.entities.KmlStyle
import com.simosc.landworkscheduler.domain.extension.parseColorAbgr
import com.simosc.landworkscheduler.domain.extension.parseLatLng
import com.simosc.landworkscheduler.domain.extension.toAbgrString
import com.simosc.landworkscheduler.domain.extension.toLongLatAltString
import com.simosc.landworkscheduler.domain.extension.trimWithSingleWhitespaces
import com.simosc.landworkscheduler.domain.model.Land

fun getAllLands(styles: List<KmlStyle>, placemarks: List<KmlPlacemark>): List<Land>{
    val result = mutableListOf<Land>()
    placemarks.forEach { placemark ->
        if(placemark.name.isNullOrBlank()) return@forEach
        if(placemark.geometry !is KmlPolygon) return@forEach
        if(placemark.geometry.outerBoundary.isEmpty()) return@forEach
        val title = placemark.name.trimWithSingleWhitespaces()
        val color = placemark.styleUrl?.let{
            styles.firstOrNull {
                placemark.styleUrl.replace("#","") == it.id
            }?.let{ style ->
                style.polyColor?.let{
                    Color.parseColorAbgr(style.polyColor).copy(alpha = 1f)
                } ?: style.lineColor?.let{
                    Color.parseColorAbgr(style.lineColor).copy(alpha = 1f)
                } ?: Land.emptyLand().color
            } ?: Land.emptyLand().color
        } ?: Land.emptyLand().color
        val border = MutableList(placemark.geometry.outerBoundary.size){ i ->
            parseLatLng(placemark.geometry.outerBoundary[i])
        }.apply {
            if(isNotEmpty() && firstOrNull() == lastOrNull()){
                removeAt(size - 1)
            }
        }.toList()
        val holes = List(placemark.geometry.innerBoundary.size){ i ->
            MutableList(placemark.geometry.innerBoundary[i].size){ j ->
                parseLatLng(placemark.geometry.innerBoundary[i][j])
            }.apply {
                if(isNotEmpty() && firstOrNull() == lastOrNull()){
                    removeAt(size - 1)
                }
            }.toList()
        }
        result.add(
            Land.emptyLand().copy(
                title = title,
                color = color,
                border = border,
                holes = holes,
            )
        )
    }
    return result.toList()
}

fun Land.getKmlRawEntities(): Pair<KmlStyle, KmlPlacemark>{
    return Pair(
        KmlStyle(
            id = id.toString(),
            lineColor = color.copy(alpha = DefaultMapItemStrokeAlpha).toAbgrString(),
            lineWidth = 1.0,
            polyColor = color.copy(alpha = DefaultMapItemFillAlpha).toAbgrString(),
        ),
        KmlPlacemark(
            name = title,
            description = null,
            styleUrl = "#$id",
            geometry = KmlPolygon(
                outerBoundary = List(border.size){ i ->
                    border[i].toLongLatAltString()
                },
                innerBoundary = List(holes.size){ i ->
                    List(holes[i].size){ j ->
                        holes[i][j].toLongLatAltString()
                    }
                }
            )
        )
    )
}

fun List<Land>.getKmlRawEntities(): Pair<List<KmlStyle>,List<KmlPlacemark>>{
    val styles = mutableListOf<KmlStyle>()
    val placemarks = mutableListOf<KmlPlacemark>()
    forEach { land ->
        land.getKmlRawEntities().let{ (style, placemark) ->
            styles.add(style)
            placemarks.add(placemark)
        }
    }
    return Pair(styles.toList(), placemarks.toList())
}


