package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

class GetZoneWorks @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(zone: Zone): Flow<List<Work>> {
        return workRepository.getZoneWorks(zone.id).cancellable()
    }

    operator fun invoke(zid: Long?): Flow<List<Work>> {
        return workRepository.getZoneWorks(zid).cancellable()
    }
}