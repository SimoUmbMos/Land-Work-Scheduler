package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalNoteRepositoryImpl (
    private val db: LocalDatabase
): LocalNoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return db.localNoteDao().getAllNotes().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLandNotes(lid: Long): Flow<List<Note>> {
        return db.localNoteDao().getAllLandNotes(lid).map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getNote(id: Long): Flow<Note> {
        return db.localNoteDao().getNote(id).map{ it.toModel() }
    }

    override fun insertNote(note: Note): Note {
        note.toEntity().let{ entity ->
            db.localNoteDao().insertNote(entity).let{ id ->
                return note.copy(id = id)
            }
        }
    }

    override fun removeNote(note: Note) {
        db.localNoteDao().deleteNote(note.toEntity())
    }

}