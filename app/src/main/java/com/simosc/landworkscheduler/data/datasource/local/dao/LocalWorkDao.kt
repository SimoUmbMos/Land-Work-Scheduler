package com.simosc.landworkscheduler.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalWorkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalWorkDao {
    @Query("SELECT * FROM works")
    fun getWorks(): Flow<List<LocalWorkEntity>>

    @Query("SELECT * FROM works WHERE lid = :lid")
    fun getLandWorks(lid: Long): Flow<List<LocalWorkEntity>>

    @Query("SELECT * FROM works WHERE zid = :zid")
    fun getZoneWorks(zid: Long?): Flow<List<LocalWorkEntity>>

    @Query("SELECT * FROM works WHERE date LIKE '%' || :date || '%'")
    fun getDateWorks(date: String): Flow<List<LocalWorkEntity>>

    @Query("SELECT COUNT(id) FROM works WHERE date LIKE '%' || :date || '%'")
    fun getDateWorksCount(date: String): Flow<Long>

    @Query("SELECT * FROM works WHERE id = :id LIMIT 1")
    fun getWork(id: Long): Flow<LocalWorkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWork(work: LocalWorkEntity): Long

    @Delete
    fun deleteWork(work: LocalWorkEntity)

    @Query("DELETE FROM works WHERE lid = :lid")
    fun deleteWorksByLandId(lid: Long)

    @Query("DELETE FROM works WHERE zid = :zid")
    fun deleteWorksByZoneId(zid: Long)
}