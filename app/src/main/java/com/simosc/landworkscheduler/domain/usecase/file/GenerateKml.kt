package com.simosc.landworkscheduler.domain.usecase.file

import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.domain.model.Land
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.OutputStream
import javax.inject.Inject

@ViewModelScoped
class GenerateKml @Inject constructor(
    private val kmlFileExporter: KmlFileExporter
){
    suspend operator fun invoke(
        lands: List<Land>,
        outputStream: OutputStream
    ): Boolean{
        return kmlFileExporter.generateKml(
            lands,
            outputStream
        )
    }
}