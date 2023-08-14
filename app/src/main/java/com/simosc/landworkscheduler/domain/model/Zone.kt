package com.simosc.landworkscheduler.domain.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class Zone(
    val id: Long,
    val lid: Long,
    val title: String,
    val color: Color,
    val border: List<LatLng>,
    val holes: List<List<LatLng>>
){

    companion object{
        fun emptyZone(
            lid: Long
        ) = Zone(
            id = 0L,
            lid = lid,
            title = "",
            color = Color(0xFFFF5722),
            border = emptyList(),
            holes = emptyList()
        )
    }
}