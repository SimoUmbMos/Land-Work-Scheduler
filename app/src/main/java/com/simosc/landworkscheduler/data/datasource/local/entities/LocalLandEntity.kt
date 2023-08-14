package com.simosc.landworkscheduler.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lands")
data class LocalLandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val color: Int,
    val border: String,
    val holes: String
)
