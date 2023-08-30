package com.simosc.landworkscheduler.data.datasource.local.extensions

import com.simosc.landworkscheduler.data.datasource.local.entities.LocalWorkEntity
import com.simosc.landworkscheduler.domain.model.Work
import java.time.LocalDateTime


fun LocalWorkEntity.toModel(): Work {
    return Work(
        id = id,
        lid = if(lid > 0) lid else null,
        zid = if(zid > 0) zid else null,
        title = title,
        desc = desc,
        date = LocalDateTime.parse(date),
        created = LocalDateTime.parse(created),
        edited = LocalDateTime.parse(edited),
    )
}

fun Work.toEntity(): LocalWorkEntity {
    return LocalWorkEntity(
        id = id,
        lid = lid ?: 0,
        zid = zid ?: 0,
        title = title,
        desc = desc,
        date = date.toString(),
        created = created.toString(),
        edited = edited.toString(),
    )
}