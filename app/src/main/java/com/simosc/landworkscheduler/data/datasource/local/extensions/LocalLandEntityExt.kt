package com.simosc.landworkscheduler.data.datasource.local.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalLandEntity
import com.simosc.landworkscheduler.domain.model.Land


fun LocalLandEntity.toModel(): Land {
    val gson = GsonBuilder().create()
    val borderType = object : TypeToken<List<LatLng>>(){}.type
    val holesType = object : TypeToken<List<List<LatLng>>>(){}.type
    return Land(
        id = id,
        title = title,
        color = Color(color),
        border = gson.fromJson(border,borderType),
        holes = gson.fromJson(holes,holesType),
    )
}

fun Land.toEntity(): LocalLandEntity {
    val gson = GsonBuilder().create()
    return LocalLandEntity(
        id = id,
        title = title,
        color = color.toArgb(),
        border = gson.toJson(border),
        holes = gson.toJson(holes),
    )
}