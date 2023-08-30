package com.simosc.landworkscheduler.presentation.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.MapsInitializer
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.domain.extension.putLand
import com.simosc.landworkscheduler.domain.files.KmlFileImporter
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlLandReaderStates
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlLandReaderViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader.KmlReaderScreenContent
import com.simosc.landworkscheduler.presentation.ui.theme.LandWorkSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KmlReaderActivity : ComponentActivity() {
    private val viewModel: KmlLandReaderViewModel by viewModels()
    private val openDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        viewModel.onOpenDocumentResult(
            uri = uri,
            resolver = contentResolver,
            onCancel = ::onCancel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandWorkSchedulerTheme {

                val systemUiController: SystemUiController = rememberSystemUiController()
                val useDarkIcons: Boolean = !isSystemInDarkTheme()
                val context: Context = LocalContext.current
                val background: Color = MaterialTheme.colorScheme.background
                val onBackground: Color = MaterialTheme.colorScheme.onBackground
                val uiState: KmlLandReaderStates by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit){
                    MapsInitializer.initialize(context)
                }

                LaunchedEffect(uiState){
                    when(uiState){
                        is KmlLandReaderStates.WaitingFile -> {
                            openDocument.launch(arrayOf(KmlFileImporter.MimeType))
                        }

                        is KmlLandReaderStates.ErrorParsing -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.kml_lands_reader_error_file_cant_parse),
                                Toast.LENGTH_LONG
                            ).show()
                            onCancel()
                        }

                        is KmlLandReaderStates.NoLandsFound -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.kml_lands_reader_error_file_cant_parse),
                                Toast.LENGTH_LONG
                            ).show()
                            onCancel()
                        }
                        else -> {}
                    }
                }

                BackHandler {
                    onCancel()
                }

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = background,
                        darkIcons = useDarkIcons
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = background,
                    contentColor = onBackground,
                    topBar = {
                        uiState.let{ state ->
                            DefaultTopAppBar(
                                title = stringResource(id = R.string.kml_lands_reader_title),
                                subTitle = when(state){
                                    is KmlLandReaderStates.LandSelected -> stringResource(
                                        id = R.string.kml_lands_reader_subtitle_selected_land,
                                        state.selectedLand.title
                                    )

                                    is KmlLandReaderStates.NoLandSelected -> stringResource(
                                        id = R.string.kml_lands_reader_subtitle_default
                                    )

                                    else -> null
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            onCancel()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.cancel_label)
                                        )
                                    }
                                },
                                actions = {
                                    if(state is KmlLandReaderStates.LoadedLands){
                                        IconButton(
                                            enabled = state is KmlLandReaderStates.LandSelected,
                                            onClick = {
                                                if(state is KmlLandReaderStates.LandSelected)
                                                    onSubmit(state.selectedLand)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = stringResource(
                                                    id = R.string.submit_label
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                ){ padding ->
                    KmlReaderScreenContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        uiState = uiState,
                        onOpenDocuments = {
                            openDocument.launch(arrayOf(KmlFileImporter.MimeType))
                        },
                        onLandClick = { land ->
                            uiState.let{ state ->
                                if( state is KmlLandReaderStates.LandSelected && state.selectedLand == land)
                                    viewModel.onClearSelectedLand()
                                else
                                    viewModel.onLandSelect(land)
                            }
                        }
                    )
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

