package com.simosc.landworkscheduler.domain.usecase.land

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

@ViewModelScoped
class GetLands @Inject constructor(
    private val landRepository: LocalLandRepository
) {
    operator fun invoke(): Flow<List<Land>>{
        return landRepository.getLands().cancellable()
    }
}