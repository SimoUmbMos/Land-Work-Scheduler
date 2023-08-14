package com.simosc.landworkscheduler.domain.client

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(
        priority: Int,
        interval: Long,
        fasterInterval: Long,
        minimalDistance: Float
    ): Flow<Location>

    suspend fun getCurrentLocation(
        priority: Int
    ): Location

    suspend fun getLastKnowLocation(): Location
}