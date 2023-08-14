package com.simosc.landworkscheduler.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteDao {

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<LocalNoteEntity>>

    @Query("SELECT * FROM notes WHERE lid = :lid")
    fun getAllLandNotes(lid: Long): Flow<List<LocalNoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNote(id: Long): Flow<LocalNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: LocalNoteEntity): Long

    @Delete
    fun deleteNote(note: LocalNoteEntity)

    @Query("DELETE FROM notes WHERE lid = :lid")
    fun deleteNotesByLandId(lid: Long)
}