package com.simosc.landworkscheduler.domain.usecase.zone

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

@ViewModelScoped
class GetLandZones @Inject constructor(
    private val zoneRepository: LocalZoneRepository
) {
    operator fun invoke(land: Land): Flow<List<Zone>> {
        return zoneRepository.getLandZones(land.id).cancellable()
    }

    operator fun invoke(lid: Long): Flow<List<Zone>>  {
        return zoneRepository.getLandZones(lid).cancellable()
    }
}