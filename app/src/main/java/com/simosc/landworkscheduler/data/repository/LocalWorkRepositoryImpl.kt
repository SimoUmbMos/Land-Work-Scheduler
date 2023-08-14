package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.dao.LocalWorkDao
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class LocalWorkRepositoryImpl (
    private val workDao: LocalWorkDao
): LocalWorkRepository {

    override fun getWorks(): Flow<List<Work>> {
        return workDao.getWorks().map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getLandWorks(lid: Long): Flow<List<Work>> {
        return workDao.getLandWorks(lid).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getZoneWorks(zid: Long?): Flow<List<Work>> {
        return workDao.getZoneWorks(zid).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getDateWorks(date: LocalDate): Flow<List<Work>> {
        return workDao.getDateWorks(date.toString()).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getDateWorksCount(date: LocalDate): Flow<Long> {
        return workDao.getDateWorksCount(date.toString())
    }

    override fun getWork(id: Long): Flow<Work?> {
        return workDao.getWork(id).map {
            it?.toModel()
        }
    }

    override fun insertWork(work: Work): Work {
        work.toEntity().let{ entity ->
            workDao.insertWork(entity).let{ id ->
                return work.copy(id = id)
            }
        }
    }

    override fun removeWork(work: Work) {
        workDao.deleteWork(work.toEntity())
    }

}