package com.simosc.landworkscheduler.domain.usecase.file

import com.simosc.landworkscheduler.domain.files.KmlFileImporter
import com.simosc.landworkscheduler.domain.model.Land
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.InputStream
import javax.inject.Inject

@ViewModelScoped
class ReadKml @Inject constructor(
    private val kmlFileImporter: KmlFileImporter
){
    suspend operator fun invoke(inputStream: InputStream): List<Land>{
        return kmlFileImporter.readLandsFromKml(inputStream)
    }
}