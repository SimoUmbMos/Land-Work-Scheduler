package com.simosc.landworkscheduler.domain.usecase.note

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

@ViewModelScoped
class GetLandNotes @Inject constructor(
    private val noteRepository: LocalNoteRepository
) {
    operator fun invoke(land: Land): Flow<List<Note>> {
        return noteRepository.getLandNotes(land.id).cancellable()
    }

    operator fun invoke(lid: Long): Flow<List<Note>> {
        return noteRepository.getLandNotes(lid).cancellable()
    }
}