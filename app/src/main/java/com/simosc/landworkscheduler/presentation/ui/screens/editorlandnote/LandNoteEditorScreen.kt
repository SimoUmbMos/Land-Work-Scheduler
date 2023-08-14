package com.simosc.landworkscheduler.presentation.ui.screens.editorlandnote

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.domain.extension.getCenter
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent

@Composable
fun LandNoteEditorScreen(
    uiState: LandNoteEditorStates,
    onBackPress: () -> Unit = {},
    onTitleUpdate: (String) -> Unit = {},
    onDescUpdate: (String) -> Unit = {},
    onColorUpdate: (Color) -> Unit = {},
    onCenterUpdate: (LatLng) -> Unit = {},
    onRadiusUpdate: (Double) -> Unit = {},
    onRevertChanges: () -> Unit = {},
    onSaveNote: () -> Unit = {},
    onDeleteNote: () -> Unit = {},
    initShowSaveDialog: Boolean = false,
    initShowColorDialog: Boolean = false,
    initShowCenterDialog: Boolean = false,
    initRevertChangesDialog: Boolean = false
){
    var showSaveDialog by remember(initShowSaveDialog){
        mutableStateOf(initShowSaveDialog)
    }
    var showColorDialog by remember(initShowColorDialog){
        mutableStateOf(initShowColorDialog)
    }
    var showCenterDialog by remember(initShowCenterDialog){
        mutableStateOf(initShowCenterDialog)
    }
    var showRevertChangesDialog by remember(initRevertChangesDialog){
        mutableStateOf(initRevertChangesDialog)
    }
    LaunchedEffect(uiState){
        if(uiState is LandNoteEditorStates.CantInit)
            onBackPress()
    }
    BackHandler(uiState is LandNoteEditorStates.NormalState && uiState.needSave()){
        showSaveDialog = true
    }
    Scaffold(
        topBar = {
            LandNoteTopBar(
                uiState = uiState,
                onBackPress = onBackPress,
                onSaveNote = onSaveNote,
                onDeleteNote = onDeleteNote
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ){
                if(uiState is LandNoteEditorStates.NormalState)
                    LandNoteContent(
                        uiState = uiState,
                        onTitleUpdate = onTitleUpdate,
                        onDescUpdate = onDescUpdate,
                        onRadiusUpdate = onRadiusUpdate
                    )
                else
                    LoadingContentComponent(text = "Note Data...")
            }
        }
    )
    LandNoteEditorDialogs(
        showSaveDialog = showSaveDialog,
        onSaveNote = onSaveNote,
        onBackPress = onBackPress,
        onSaveDismiss = {showSaveDialog = false},

        showColorDialog = showColorDialog,
        onColorUpdate = onColorUpdate,
        onColorDismiss = {showColorDialog = false},

        showCenterDialog = showCenterDialog,
        onCenterUpdate = onCenterUpdate,
        onCenterDismiss = {showCenterDialog = false},

        showRevertChangesDialog = showRevertChangesDialog,
        onRevertChanges = onRevertChanges,
        onRevertChangesDismiss = {showRevertChangesDialog = false},
    )
}

@Composable
private fun LandNoteTopBar(
    uiState: LandNoteEditorStates,
    onBackPress: () -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: () -> Unit
) {
    TODO("Not yet implemented")
}

@Composable
private fun LandNoteContent(
    uiState: LandNoteEditorStates.NormalState,
    onTitleUpdate: (String) -> Unit,
    onDescUpdate: (String) -> Unit,
    onRadiusUpdate: (Double) -> Unit
) {
    TODO("Not yet implemented")
}

@Composable
private fun LandNoteEditorDialogs(
    showSaveDialog: Boolean,
    showColorDialog: Boolean,
    showCenterDialog: Boolean,
    showRevertChangesDialog: Boolean,
    onSaveNote: () -> Unit,
    onBackPress: () -> Unit,
    onColorUpdate: (Color) -> Unit,
    onCenterUpdate: (LatLng) -> Unit,
    onRevertChanges: () -> Unit,
    onSaveDismiss: () -> Unit,
    onColorDismiss: () -> Unit,
    onCenterDismiss: () -> Unit,
    onRevertChangesDismiss: () -> Unit,
) {
    when{
        showSaveDialog -> {
            TODO("Not yet implemented")
        }
        showColorDialog -> {
            TODO("Not yet implemented")
        }
        showCenterDialog -> {
            TODO("Not yet implemented")
        }
        showRevertChangesDialog -> {
            TODO("Not yet implemented")
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewLandNoteEditorScreenLoading(){
    LandNoteEditorScreen(
        uiState = LandNoteEditorStates.LoadingState
    )
}
@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewLandNoteEditorScreenCantInit(){
    LandNoteEditorScreen(
        uiState = LandNoteEditorStates.CantInit
    )
}
@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewLandNoteEditorScreenNormal(){
    val land = Land.emptyLand()
    val note = Note.emptyNote(
        lid = land.id,
        center = land.border.getCenter() ?: LatLng(0.0,0.0)
    )
    LandNoteEditorScreen(
        uiState = LandNoteEditorStates.NormalState(
            land = land,
            note = note
        )
    )
}
