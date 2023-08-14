package com.simosc.landworkscheduler.data.client

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.domain.client.GeoClient
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GeoClientImpl(
    private val geocoder: Geocoder
): GeoClient {

    constructor(
        context: Context
    ):this(Geocoder(context))

    @Suppress("DEPRECATION")
    override suspend fun getAddress(address: String): List<Address> = suspendCoroutine { cont ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(
                address,
                1
            ) { addresses -> cont.resume(addresses) }
        } else {
            (geocoder.getFromLocationName(address,1) ?: emptyList()).let{
                cont.resume(it)
            }
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getAddress(location: Location): List<Address> = suspendCoroutine { cont ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude,1){
                cont.resume(it)
            }
        } else {
            cont.resume(
                geocoder.getFromLocation(location.latitude, location.longitude,1) ?:
                emptyList()
            )
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getAddress(latLng: LatLng): List<Address> = suspendCoroutine { cont ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude,1){
                cont.resume(it)
            }
        } else {
            cont.resume(
                geocoder.getFromLocation(latLng.latitude, latLng.longitude,1) ?:
                emptyList()
            )
        }
    }
}