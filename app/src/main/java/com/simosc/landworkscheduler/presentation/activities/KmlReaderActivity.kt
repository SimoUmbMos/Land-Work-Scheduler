package com.simosc.landworkscheduler.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.MapsInitializer
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.domain.extension.putLand
import com.simosc.landworkscheduler.domain.files.KmlFileImporter
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlLandReaderScreen
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlLandReaderStates
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlLandReaderViewModel
import com.simosc.landworkscheduler.presentation.ui.theme.LandWorkSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KmlReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this)
        setContent {
            LandWorkSchedulerTheme {
                val background = MaterialTheme.colorScheme.background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = background
                ) {
                    val systemUiController = rememberSystemUiController()
                    val useDarkIcons = !isSystemInDarkTheme()
                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = background,
                            darkIcons = useDarkIcons
                        )
                    }

                    val viewModel = hiltViewModel<KmlLandReaderViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    val fileExplorer = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ){ selectedFile ->
                        selectedFile?.run{
                            lifecycleScope.launch(Dispatchers.IO){
                                contentResolver.openInputStream(selectedFile)?.use{
                                    viewModel.parseFile(it)
                                }?: onCancel()
                            }
                        } ?: onCancel()
                    }

                    LaunchedEffect(uiState){
                        when(uiState){
                            KmlLandReaderStates.ErrorParsing -> {
                                Toast.makeText(
                                    this@KmlReaderActivity,
                                    getString(R.string.kml_lands_reader_error_file_cant_parse),
                                    Toast.LENGTH_LONG
                                ).show()
                                onCancel()
                            }

                            KmlLandReaderStates.NoLandsFound -> {
                                Toast.makeText(
                                    this@KmlReaderActivity,
                                    getString(R.string.kml_lands_reader_error_file_cant_parse),
                                    Toast.LENGTH_LONG
                                ).show()
                                onCancel()
                            }

                            else -> {}
                        }
                    }

                    KmlLandReaderScreen(
                        uiState = uiState,
                        cameraPositionState = viewModel.cameraPositionState,
                        onOpenFileExplorer = {
                            fileExplorer.launch(arrayOf(KmlFileImporter.MimeType))
                        },
                        onLandSelect = viewModel::onLandSelect,
                        onClearSelectedLand = viewModel::onClearSelectedLand,
                        onSubmit = ::onSubmit,
                        onCancel = ::onCancel,
                    )

                    LaunchedEffect(Unit){
                        fileExplorer.launch(arrayOf(KmlFileImporter.MimeType))
                    }
                }
            }
        }
    }

    private fun onSubmit(land: Land){
        setResult(RESULT_OK, Intent().apply{ putLand("export_land",land) })
        finish()
    }

    private fun onCancel(){
        setResult(RESULT_CANCELED)
        finish()
    }
}

