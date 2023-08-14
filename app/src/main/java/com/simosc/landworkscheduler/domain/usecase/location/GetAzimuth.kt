package com.simosc.landworkscheduler.domain.usecase.location

import com.simosc.landworkscheduler.domain.client.CompassClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAzimuth @Inject constructor(
    private val compassClient: CompassClient
) {
    operator fun invoke(
        interval: Long = 5000L,
        fasterInterval: Long = 1000L,
        minDegreesDiff: Float = 11.25f
    ): Flow<Float> {
        return compassClient.getBearingUpdates(
            interval = interval,
            fasterInterval = fasterInterval,
            minDegreesDiff = minDegreesDiff
        )
    }
}