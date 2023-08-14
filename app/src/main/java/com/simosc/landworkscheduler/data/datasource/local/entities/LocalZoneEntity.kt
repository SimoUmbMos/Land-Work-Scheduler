package com.simosc.landworkscheduler.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class LocalZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val lid: Long,
    val title: String,
    val color: Int,
    val border: String,
    val holes: String
)