package com.simosc.landworkscheduler.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class LocalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val lid: Long,
    val title: String,
    val desc: String,
    val color: Int,
    val centerLat: Double,
    val centerLong: Double,
    val radius: Double,
    val created: String,
    val edited: String,
)
