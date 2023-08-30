package com.simosc.landworkscheduler.domain.usecase.zone

import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

@ViewModelScoped
class GetZone @Inject constructor(
    private val zoneRepository: LocalZoneRepository
) {
    operator fun invoke(
        id: Long
    ): Flow<Zone?> {
        return zoneRepository.getZone(id).cancellable()
    }
}