package com.simosc.landworkscheduler.domain.usecase.note

import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import javax.inject.Inject

class InsertNote @Inject constructor(
    private val noteRepository: LocalNoteRepository
) {
    operator fun invoke(note: Note): Note{
        return noteRepository.insertNote(note)
    }
}