package com.simosc.landworkscheduler.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "works")
data class LocalWorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val lid: Long,
    val zid: Long,
    val title: String,
    val desc: String,
    val date: String,
    val created: String,
    val edited: String,
)