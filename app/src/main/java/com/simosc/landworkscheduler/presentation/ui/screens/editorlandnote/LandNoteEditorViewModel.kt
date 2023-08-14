package com.simosc.landworkscheduler.presentation.ui.screens.editorlandnote

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.domain.extension.getCenter
import com.simosc.landworkscheduler.domain.extension.isInside
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
import com.simosc.landworkscheduler.domain.usecase.note.DeleteNote
import com.simosc.landworkscheduler.domain.usecase.note.GetNote
import com.simosc.landworkscheduler.domain.usecase.note.InsertNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LandNoteEditorViewModel @Inject constructor(
    private val getLand: GetLand,
    private val getNote: GetNote,
    private val insertNote: InsertNote,
    private val deleteNote: DeleteNote
): ViewModel() {
    private var syncLandJob: Job? = null
    private var syncNoteJob: Job? = null

    private val _uiState: MutableStateFlow<LandNoteEditorStates> =
        MutableStateFlow(LandNoteEditorStates.LoadingState)
    private val _landId: MutableStateFlow<Long> =
        MutableStateFlow(-1L)
    private val _noteId: MutableStateFlow<Long> =
        MutableStateFlow(-1L)

    private fun stopLandSync(){
        syncLandJob?.let{
            it.cancel()
            syncLandJob = null
        }
    }

    private fun stopNoteSync(){
        syncNoteJob?.let{
            it.cancel()
            syncNoteJob = null
        }
    }

    private fun syncData(){
        stopLandSync()
        stopNoteSync()
        syncLandJob = getLand(_landId.value).onEach {
            stopNoteSync()
            it?.let{ land ->
                syncNoteJob = getNote(_noteId.value).onEach { note ->
                    _uiState.update {
                        LandNoteEditorStates.NormalState(
                            land = land,
                            note = note ?: Note.emptyNote(
                                lid = land.id,
                                center = land.border.getCenter() ?: DefaultMapTarget
                            )
                        )
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))
            }?:run{
                _uiState.update {
                    LandNoteEditorStates.CantInit
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    override fun onCleared() {
        super.onCleared()
        stopLandSync()
        stopNoteSync()
    }

    val uiState: StateFlow<LandNoteEditorStates>
        get() = _uiState.asStateFlow()

    fun setDataIds(
        landId: Long,
        noteId: Long
    ) {
        _landId.update { if(landId > 0L) landId else 0L}
        _noteId.update { if(noteId > 0L) noteId else 0L}
        syncData()
    }

    fun onTitleUpdate(newTitle: String){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState) {
                _uiState.update { state.copy(newTitle = newTitle) }
            }
        }
    }

    fun onDescUpdate(newDesc: String){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState) {
                _uiState.update { state.copy(newDesc = newDesc) }
            }
        }
    }

    fun onColorUpdate(newColor: Color){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState) {
                _uiState.update { state.copy(newColor = newColor) }
            }
        }
    }

    fun onCenterUpdate(newCenter: LatLng){
        _uiState.value.let { state ->
            if(
                state is LandNoteEditorStates.NormalState &&
                newCenter.isInside(state.land.border)
            ) {
                _uiState.update { state.copy(newCenter = newCenter) }
            }
        }
    }

    fun onRadiusUpdate(newRadius: Double){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState) {
                _uiState.update { state.copy(newRadius = newRadius) }
            }
        }
    }

    fun onRevertChanges(){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState) {
                _uiState.update { state.revertChanges() }
            }
        }
    }

    suspend fun saveNote(): Boolean{
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState){
                return viewModelScope.async(Dispatchers.IO) {
                    if(state.note.id > 0L){
                        insertNote(state.getNewNote())
                        true
                    }else{
                        insertNote(state.getNewNote()).let{ newNote ->
                            _noteId.update { newNote.id }
                            syncData()
                        }
                        true
                    }
                }.await()
            }
        }
        return false
    }

    suspend fun deleteNote(){
        _uiState.value.let { state ->
            if(state is LandNoteEditorStates.NormalState && state.note.id > 0L){
                viewModelScope.async(Dispatchers.IO) {
                    stopNoteSync()
                    deleteNote(state.note)
                    _noteId.update { 0L }
                    _uiState.update {
                        LandNoteEditorStates.NormalState(
                            land = state.land,
                            note = Note.emptyNote(
                                lid = state.land.id,
                                center = state.land.border.getCenter() ?: DefaultMapTarget
                            )
                        )
                    }
                }.await()
            }
        }
    }

}