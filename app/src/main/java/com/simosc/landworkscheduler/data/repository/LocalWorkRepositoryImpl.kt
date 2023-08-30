package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.repository.LocalWorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class LocalWorkRepositoryImpl (
    private val db: LocalDatabase
): LocalWorkRepository {

    override fun getWorks(): Flow<List<Work>> {
        return db.localWorkDao().getWorks().map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getLandWorks(lid: Long): Flow<List<Work>> {
        return db.localWorkDao().getLandWorks(lid).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getZoneWorks(zid: Long): Flow<List<Work>> {
        return db.localWorkDao().getZoneWorks(zid).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getDateWorks(date: LocalDate): Flow<List<Work>> {
        return db.localWorkDao().getDateWorks(date.toString()).map { entities ->
            List(entities.size){
                entities[it].toModel()
            }
        }
    }

    override fun getDateWorksCount(date: LocalDate): Flow<Long> {
        return db.localWorkDao().getDateWorksCount(date.toString())
    }

    override fun getWork(id: Long): Flow<Work?> {
        return db.localWorkDao().getWork(id).map {
            it?.toModel()
        }
    }

    override fun insertWork(work: Work): Work {
        work.toEntity().let{ entity ->
            db.localWorkDao().insertWork(entity).let{ id ->
                return work.copy(id = id)
            }
        }
    }

    override fun removeWork(work: Work) {
        db.localWorkDao().deleteWork(work.toEntity())
    }

}