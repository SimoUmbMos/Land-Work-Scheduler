package com.simosc.landworkscheduler.domain.extension

import android.location.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

fun Location.toLatLng(): LatLng =
    LatLng(latitude,longitude)

fun Location.isInside(border: List<LatLng>) =
    PolyUtil.containsLocation(
        toLatLng(),
        border,
        true
    )

fun LatLng.toLongLatAltString() =
    "$longitude,$latitude,0"

fun parseLatLng(coordinates: String): LatLng {
    coordinates.trim().replace(" ","").split(",").let{
        if(it.size == 2 || it.size == 3) {
            val latitude = it[1].toDoubleOrNull()
            val longitude = it[0].toDoubleOrNull()
            if( latitude != null && longitude != null)
                return LatLng(latitude,longitude)
        }
    }
    throw IllegalArgumentException()
}

fun LatLng.isInside(border: List<LatLng>) =
    PolyUtil.containsLocation(
        this,
        border,
        true
    )

fun LatLng.distanceTo(that: LatLng) =
    SphericalUtil.computeDistanceBetween(
        this,
        that
    )

fun List<LatLng>.toLatLngBounds() =
    if(isNotEmpty()){
        LatLngBounds.builder().let { builder ->
            forEach(builder::include)
            builder.build()
        }
    }else{
        null
    }

fun List<LatLng>.getCenter() =
    toLatLngBounds()?.center


fun CameraPosition.calcRadiusFromZoom(): Double =
    if(zoom < 19f)
        (195.0).minus(zoom.times(10))
    else
        5.0