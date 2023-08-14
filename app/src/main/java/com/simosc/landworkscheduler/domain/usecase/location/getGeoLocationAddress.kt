package com.simosc.landworkscheduler.domain.usecase.location

import android.location.Address
import com.simosc.landworkscheduler.domain.client.GeoClient
import javax.inject.Inject

class getGeoLocationAddress @Inject constructor(
    private val geoClient: GeoClient
) {
    suspend operator fun invoke(address: String): List<Address>{
        return geoClient.getAddress(address)
    }
}