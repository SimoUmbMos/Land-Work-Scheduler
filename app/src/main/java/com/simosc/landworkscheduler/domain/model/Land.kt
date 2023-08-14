package com.simosc.landworkscheduler.domain.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class Land(
    val id: Long,
    val title: String,
    val color: Color,
    val border: List<LatLng>,
    val holes: List<List<LatLng>>
){
    companion object{
        fun emptyLand() = Land(
            id = 0L,
            title = "",
            color = Color(0xFF8BC34A),
            border = emptyList(),
            holes = emptyList(),
        )
    }
}
