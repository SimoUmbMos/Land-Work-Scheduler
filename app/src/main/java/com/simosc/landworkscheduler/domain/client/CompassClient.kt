package com.simosc.landworkscheduler.domain.client

import kotlinx.coroutines.flow.Flow

interface CompassClient {
    fun getBearingUpdates(
        interval: Long,
        fasterInterval: Long,
        minDegreesDiff: Float,
    ): Flow<Float>
}