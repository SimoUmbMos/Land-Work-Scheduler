package com.simosc.landworkscheduler.domain.usecase.location

import android.location.Location
import com.simosc.landworkscheduler.domain.client.LocationClient
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class GetLastKnowLocation @Inject constructor(
    private val locationClient: LocationClient
) {
    suspend operator fun invoke(): Location{
        return locationClient.getLastKnowLocation()
    }
}