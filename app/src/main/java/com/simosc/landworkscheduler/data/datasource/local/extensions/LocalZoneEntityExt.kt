package com.simosc.landworkscheduler.data.datasource.local.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalZoneEntity
import com.simosc.landworkscheduler.domain.model.Zone

fun LocalZoneEntity.toModel(): Zone {
    val gson = GsonBuilder().create()
    val borderType = object : TypeToken<List<LatLng>>(){}.type
    val holesType = object : TypeToken<List<List<LatLng>>>(){}.type
    return Zone(
        id = id,
        lid = lid,
        title = title,
        color = Color(color),
        border = gson.fromJson(border,borderType),
        holes = gson.fromJson(holes,holesType),
    )
}

fun Zone.toEntity(): LocalZoneEntity {
    val gson = GsonBuilder().create()
    return LocalZoneEntity(
        id = id,
        lid = lid,
        title = title,
        color = color.toArgb(),
        border = gson.toJson(border),
        holes = gson.toJson(holes),
    )
}
