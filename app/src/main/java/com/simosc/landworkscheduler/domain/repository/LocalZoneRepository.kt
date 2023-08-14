package com.simosc.landworkscheduler.domain.repository

import com.simosc.landworkscheduler.domain.model.Zone
import kotlinx.coroutines.flow.Flow

interface LocalZoneRepository {

    fun getZones(): Flow<List<Zone>>

    fun getLandZones(lid: Long): Flow<List<Zone>>

    fun getZone(id: Long): Flow<Zone>

    fun insertZone(zone: Zone): Zone

    fun removeZone(zone: Zone)

}