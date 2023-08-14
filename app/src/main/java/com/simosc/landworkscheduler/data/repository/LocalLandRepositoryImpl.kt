package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.dao.LocalLandDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalNoteDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalWorkDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalZoneDao
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.exception.BorderListException
import com.simosc.landworkscheduler.domain.exception.TitleException
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class LocalLandRepositoryImpl (
    private val landDao: LocalLandDao,
    private val zoneDao: LocalZoneDao,
    private val noteDao: LocalNoteDao,
    private val workDao: LocalWorkDao,
): LocalLandRepository {

    override fun getLands(): Flow<List<Land>> {
        return landDao.getAllLands().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLand(id: Long): Flow<Land?> {
        return landDao.getLand(id).map { it?.toModel() }
    }

    override fun insertLand(land: Land): Land {
        if(land.border.size < 3){
            throw BorderListException()
        }else if(land.title.isBlank()){
            throw TitleException()
        }else{
            land.toEntity().let{ entity ->
                landDao.insertLand(entity).let{ id ->
                    return land.copy(id = id)
                }
            }
        }
    }

    override fun removeLand(land: Land) {
        workDao.deleteWorksByLandId(land.id)
        noteDao.deleteNotesByLandId(land.id)
        zoneDao.deleteZonesByLandId(land.id)
        landDao.deleteLand(land.toEntity())
    }

}