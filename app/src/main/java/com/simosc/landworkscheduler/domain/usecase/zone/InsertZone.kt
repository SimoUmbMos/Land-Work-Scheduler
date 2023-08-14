package com.simosc.landworkscheduler.domain.usecase.zone

import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import javax.inject.Inject

class InsertZone @Inject constructor(
    private val zoneRepository: LocalZoneRepository
) {
    operator fun invoke(zone: Zone): Zone {
        return zoneRepository.insertZone(zone)
    }
}