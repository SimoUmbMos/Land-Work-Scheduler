package com.simosc.landworkscheduler.domain.repository

import com.simosc.landworkscheduler.domain.model.Land
import kotlinx.coroutines.flow.Flow

interface LocalLandRepository {
    fun getLands(): Flow<List<Land>>

    fun getLand(id: Long): Flow<Land?>

    fun insertLand(land: Land): Land

    fun removeLand(land: Land)
}