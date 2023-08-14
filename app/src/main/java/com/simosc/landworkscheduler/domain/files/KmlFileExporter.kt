package com.simosc.landworkscheduler.domain.files

import com.simosc.landworkscheduler.domain.model.Land

interface KmlFileExporter {
    fun generateKmlString(land: Land): String
    fun generateKmlString(lands: List<Land>): String
}