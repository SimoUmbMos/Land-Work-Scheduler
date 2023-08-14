package com.simosc.landworkscheduler.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalZoneDao {

    @Query("SELECT * FROM zones")
    fun getAllZones(): Flow<List<LocalZoneEntity>>

    @Query("SELECT * FROM zones WHERE lid = :lid")
    fun getAllLandZones(lid: Long): Flow<List<LocalZoneEntity>>

    @Query("SELECT * FROM zones WHERE id = :id LIMIT 1")
    fun getZone(id: Long): Flow<LocalZoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertZone(zone: LocalZoneEntity): Long

    @Delete
    fun deleteZone(zone: LocalZoneEntity)

    @Query("DELETE FROM zones WHERE lid = :lid")
    fun deleteZonesByLandId(lid: Long)
}