package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

@ViewModelScoped
class GetWorks @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(): Flow<List<Work>> {
        return workRepository.getWorks().cancellable()
    }
}