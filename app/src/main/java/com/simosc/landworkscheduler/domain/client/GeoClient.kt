package com.simosc.landworkscheduler.domain.client

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng

interface GeoClient {
    suspend fun getAddress(
        address: String
    ): List<Address>
    suspend fun getAddress(
        location: Location
    ): List<Address>
    suspend fun getAddress(
        latLng: LatLng
    ): List<Address>
}