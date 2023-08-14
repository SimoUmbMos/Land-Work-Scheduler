package com.simosc.landworkscheduler.data.datasource.files.kml.entities

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class KmlPolygonPlacemark(
    val id: Long,
    val name: String,
    val color: Color,
    val lineWidth: Int = 1,
    val outerBoundary: List<LatLng>,
    val innerBoundary: List<List<LatLng>> = emptyList()
){

}