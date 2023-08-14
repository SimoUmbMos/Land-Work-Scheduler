package com.simosc.landworkscheduler.domain.usecase.land

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import javax.inject.Inject

class DeleteLand @Inject constructor(
    private val landRepository: LocalLandRepository
) {
    operator fun invoke(land: Land){
        landRepository.removeLand(land)
    }
}