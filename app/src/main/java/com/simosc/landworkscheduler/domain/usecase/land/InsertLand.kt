package com.simosc.landworkscheduler.domain.usecase.land

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class InsertLand @Inject constructor(
    private val landRepository: LocalLandRepository
){
    operator fun invoke(land:Land): Land {
        return landRepository.insertLand(land)
    }
}