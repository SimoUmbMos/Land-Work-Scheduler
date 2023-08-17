package com.simosc.landworkscheduler.domain.files

import com.simosc.landworkscheduler.domain.model.Land
import java.io.OutputStream

interface KmlFileExporter {

    suspend fun generateKml(
        lands: List<Land>,
        outputStream: OutputStream
    ): Boolean

}