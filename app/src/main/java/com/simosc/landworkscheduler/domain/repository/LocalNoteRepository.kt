package com.simosc.landworkscheduler.domain.repository

import com.simosc.landworkscheduler.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface LocalNoteRepository {

    fun getNotes(): Flow<List<Note>>

    fun getLandNotes(lid: Long): Flow<List<Note>>

    fun getNote(id: Long): Flow<Note>

    fun insertNote(note: Note): Note

    fun removeNote(note: Note)

}