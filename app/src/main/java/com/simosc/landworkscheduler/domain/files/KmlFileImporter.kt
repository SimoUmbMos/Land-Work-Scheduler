package com.simosc.landworkscheduler.domain.files

import com.simosc.landworkscheduler.domain.model.Land
import java.io.InputStream

interface KmlFileImporter {
    suspend fun readLandsFromKml(
        inputStream: InputStream
    ): List<Land>

}