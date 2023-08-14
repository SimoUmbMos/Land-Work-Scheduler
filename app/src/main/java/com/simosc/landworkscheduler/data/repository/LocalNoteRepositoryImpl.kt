package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.dao.LocalNoteDao
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.repository.LocalNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalNoteRepositoryImpl (
    private val noteDao: LocalNoteDao
): LocalNoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLandNotes(lid: Long): Flow<List<Note>> {
        return noteDao.getAllLandNotes(lid).map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getNote(id: Long): Flow<Note> {
        return noteDao.getNote(id).map{ it.toModel() }
    }

    override fun insertNote(note: Note): Note {
        note.toEntity().let{ entity ->
            noteDao.insertNote(entity).let{ id ->
                return note.copy(id = id)
            }
        }
    }

    override fun removeNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

}