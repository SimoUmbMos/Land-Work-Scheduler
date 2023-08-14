package com.simosc.landworkscheduler.domain.usecase.work

import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import javax.inject.Inject

class DeleteWork @Inject constructor(
    private val workRepository: LocalWorkRepository
) {
    operator fun invoke(work: Work) {
        return workRepository.removeWork(work)
    }
}