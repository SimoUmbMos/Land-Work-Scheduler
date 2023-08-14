package com.simosc.landworkscheduler.domain.usecase.location

import android.location.Location
import com.google.android.gms.location.Priority
import com.simosc.landworkscheduler.domain.client.LocationClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocation @Inject constructor(
    private val locationClient: LocationClient,
) {
    operator fun invoke(
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
        interval: Long = 5000L,
        fasterInterval: Long = 1000L,
        minimalDistance: Float = 1.0f
    ): Flow<Location> {
        return locationClient.getLocationUpdates(
            priority = priority,
            interval = interval,
            fasterInterval = fasterInterval,
            minimalDistance = minimalDistance,
        )
    }
}