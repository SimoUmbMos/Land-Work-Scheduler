package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.database.LocalDatabase
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalZoneRepositoryImpl(
    private val db: LocalDatabase
): LocalZoneRepository {

    override fun getZones(): Flow<List<Zone>> {
        return db.localZoneDao().getAllZones().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLandZones(lid: Long): Flow<List<Zone>> {
        return db.localZoneDao().getAllLandZones(lid).map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getZone(id: Long): Flow<Zone> {
        return db.localZoneDao().getZone(id).map { it.toModel() }
    }

    override fun insertZone(zone: Zone): Zone {
        zone.toEntity().let { entity ->
            db.localZoneDao().insertZone(entity).let{ id ->
                return zone.copy(id = id)
            }
        }
    }

    override fun removeZone(zone: Zone) {
        db.localWorkDao().deleteWorksByZoneId(zone.id)
        db.localZoneDao().deleteZone(zone.toEntity())
    }

}