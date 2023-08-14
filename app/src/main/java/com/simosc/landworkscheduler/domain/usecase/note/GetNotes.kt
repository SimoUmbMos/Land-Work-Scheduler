package com.simosc.landworkscheduler.domain.usecase.note

import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

class GetNotes @Inject constructor(
    private val noteRepository: LocalNoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return noteRepository.getNotes().cancellable()
    }
}