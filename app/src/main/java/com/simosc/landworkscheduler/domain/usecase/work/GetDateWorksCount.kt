package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import java.time.LocalDate
import javax.inject.Inject

@ViewModelScoped
class GetDateWorksCount @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(date: LocalDate): Flow<Long> {
        return workRepository.getDateWorksCount(date).cancellable()
    }
}