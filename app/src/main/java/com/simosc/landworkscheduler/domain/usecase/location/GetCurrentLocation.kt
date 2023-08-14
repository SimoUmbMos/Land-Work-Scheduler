package com.simosc.landworkscheduler.domain.usecase.location

import android.location.Location
import com.google.android.gms.location.Priority
import com.simosc.landworkscheduler.domain.client.LocationClient
import javax.inject.Inject

class GetCurrentLocation @Inject constructor(
    private val locationClient: LocationClient
) {
    suspend operator fun invoke(
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY
    ): Location{
        return locationClient.getCurrentLocation(priority)
    }
}