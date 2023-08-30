package com.simosc.landworkscheduler.data.datasource.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalLandDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalNoteDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalWorkDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalZoneDao
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalLandEntity
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalNoteEntity
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalWorkEntity
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalZoneEntity

@Database(
    version = LocalDatabase.database_version,
    entities = [
        LocalLandEntity::class,
        LocalZoneEntity::class,
        LocalNoteEntity::class,
        LocalWorkEntity::class,
    ],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class LocalDatabase: RoomDatabase() {
    companion object{
        const val database_name = "land_work_scheduler_db"
        const val database_version = 2
    }
    abstract fun localLandDao(): LocalLandDao
    abstract fun localZoneDao(): LocalZoneDao
    abstract fun localNoteDao(): LocalNoteDao
    abstract fun localWorkDao(): LocalWorkDao
}