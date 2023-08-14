package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import java.time.LocalDate
import javax.inject.Inject

class GetDateWorks @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(data: LocalDate): Flow<List<Work>> {
        return workRepository.getDateWorks(data).cancellable()
    }
}