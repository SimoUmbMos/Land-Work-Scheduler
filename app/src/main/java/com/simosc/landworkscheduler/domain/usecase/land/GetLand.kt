package com.simosc.landworkscheduler.domain.usecase.land

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

class GetLand @Inject constructor(
    private val localLandRepository: LocalLandRepository
) {
    operator fun invoke(
        id: Long
    ): Flow<Land?>{
        return localLandRepository.getLand(id).cancellable()
    }
}