package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import java.time.LocalDate
import javax.inject.Inject

class GetDateWorksCount @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(date: LocalDate): Flow<Long> {
        return workRepository.getDateWorksCount(date).cancellable()
    }
}