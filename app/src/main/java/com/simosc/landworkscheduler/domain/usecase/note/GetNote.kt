package com.simosc.landworkscheduler.domain.usecase.note

import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

class GetNote @Inject constructor(
    private val noteRepository: LocalNoteRepository
) {
    operator fun invoke(id: Long): Flow<Note?> {
        return noteRepository.getNote(id).cancellable()
    }
}