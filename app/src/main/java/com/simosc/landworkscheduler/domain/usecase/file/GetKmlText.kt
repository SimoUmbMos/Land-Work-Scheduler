package com.simosc.landworkscheduler.domain.usecase.file

import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.domain.model.Land
import javax.inject.Inject

class GetKmlText @Inject constructor(
    private val kmlFileExporter: KmlFileExporter
){
    operator fun invoke(
        lands: List<Land>
    ): String{
        return kmlFileExporter.generateKmlString(lands)
    }
}