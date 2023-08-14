package com.simosc.landworkscheduler.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalLandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalLandDao {

    @Query("SELECT * FROM lands")
    fun getAllLands(): Flow<List<LocalLandEntity>>

    @Query("SELECT * FROM lands WHERE id = :id LIMIT 1")
    fun getLand(id: Long): Flow<LocalLandEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLand(land: LocalLandEntity): Long

    @Delete
    fun deleteLand(land: LocalLandEntity)
}