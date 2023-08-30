package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.exception.BorderListException
import com.simosc.landworkscheduler.domain.exception.TitleException
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.repository.LocalLandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class LocalLandRepositoryImpl (
    private val db: LocalDatabase
): LocalLandRepository {

    override fun getLands(): Flow<List<Land>> {
        return db.localLandDao().getAllLands().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLand(id: Long): Flow<Land?> {
        return db.localLandDao().getLand(id).map { it?.toModel() }
    }

    override fun insertLand(land: Land): Land {
        if(land.border.size < 3){
            throw BorderListException()
        }else if(land.title.isBlank()){
            throw TitleException()
        }else{
            land.toEntity().let{ entity ->
                db.localLandDao().insertLand(entity).let{ id ->
                    return land.copy(id = id)
                }
            }
        }
    }

    override fun removeLand(land: Land) {
        db.localWorkDao().deleteWorksByLandId(land.id)
        db.localNoteDao().deleteNotesByLandId(land.id)
        db.localZoneDao().deleteZonesByLandId(land.id)
        db.localLandDao().deleteLand(land.toEntity())
    }

}