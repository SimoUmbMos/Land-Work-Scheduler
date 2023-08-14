package com.simosc.landworkscheduler.data.repository

import com.simosc.landworkscheduler.data.datasource.local.dao.LocalWorkDao
import com.simosc.landworkscheduler.data.datasource.local.dao.LocalZoneDao
import com.simosc.landworkscheduler.data.datasource.local.extensions.toEntity
import com.simosc.landworkscheduler.data.datasource.local.extensions.toModel
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.repository.LocalZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalZoneRepositoryImpl(
    private val zoneDao: LocalZoneDao,
    private val workDao: LocalWorkDao
): LocalZoneRepository {

    override fun getZones(): Flow<List<Zone>> {
        return zoneDao.getAllZones().map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getLandZones(lid: Long): Flow<List<Zone>> {
        return zoneDao.getAllLandZones(lid).map { entities ->
            List(entities.size){ index ->
                entities[index].toModel()
            }
        }
    }

    override fun getZone(id: Long): Flow<Zone> {
        return zoneDao.getZone(id).map { it.toModel() }
    }

    override fun insertZone(zone: Zone): Zone {
        zone.toEntity().let { entity ->
            zoneDao.insertZone(entity).let{ id ->
                return zone.copy(id = id)
            }
        }
    }

    override fun removeZone(zone: Zone) {
        workDao.deleteWorksByZoneId(zone.id)
        zoneDao.deleteZone(zone.toEntity())
    }

}