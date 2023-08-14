package com.simosc.landworkscheduler.domain.usecase.location

import android.location.Location
import com.simosc.landworkscheduler.domain.client.LocationClient
import javax.inject.Inject

class GetLastKnowLocation @Inject constructor(
    private val locationClient: LocationClient
) {
    suspend operator fun invoke(): Location{
        return locationClient.getLastKnowLocation()
    }
}