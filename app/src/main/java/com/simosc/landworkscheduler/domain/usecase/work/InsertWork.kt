package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import javax.inject.Inject

class InsertWork @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(work: Work): Work {
        return workRepository.insertWork(work)
    }
}