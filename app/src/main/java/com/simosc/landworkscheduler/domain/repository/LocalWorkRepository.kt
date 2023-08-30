package com.simosc.landworkscheduler.domain.repository

import com.simosc.landworkscheduler.domain.model.Work
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LocalWorkRepository {
    fun getWorks(): Flow<List<Work>>

    fun getLandWorks(lid: Long): Flow<List<Work>>

    fun getZoneWorks(zid: Long): Flow<List<Work>>

    fun getDateWorks(date: LocalDate): Flow<List<Work>>

    fun getDateWorksCount(date: LocalDate): Flow<Long>

    fun getWork(id: Long): Flow<Work?>

    fun insertWork(work: Work): Work

    fun removeWork(work: Work)

}