package com.simosc.landworkscheduler.domain.model

import java.time.LocalDateTime

data class Work(
    val id: Long,
    val lid: Long?,
    val zid: Long?,
    val title: String,
    val desc: String,
    val date: LocalDateTime,
    val created: LocalDateTime,
    val edited: LocalDateTime,
) {
    companion object {
        fun emptyWork(
            lid: Long?,
            zid: Long?
        ) = Work(
            id = 0L,
            lid = lid,
            zid = zid,
            title = "",
            desc = "",
            date = LocalDateTime.now().plusHours(1L),
            created = LocalDateTime.now(),
            edited = LocalDateTime.now()
        )
    }
}