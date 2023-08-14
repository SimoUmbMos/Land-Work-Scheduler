package com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.extension.tokenizedSearchIn
import com.simosc.landworkscheduler.domain.extension.trimWithSingleWhitespaces
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
import com.simosc.landworkscheduler.domain.usecase.note.GetLandNotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LandNotesMenuViewModel @Inject constructor(
    private val getLandUseCase: GetLand,
    private val getLandNotesUseCase: GetLandNotes
):ViewModel() {

    override fun onCleared() {
        super.onCleared()
        stopAllSync()
    }

    private var landSyncJob: Job? = null
    private var notesSyncJob: Job? = null

    private val _searchQuery: MutableStateFlow<String> =
        MutableStateFlow("")
    private val _isSearchLoading: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val _dataState: MutableStateFlow<LandNotesMenuStates> =
        MutableStateFlow(LandNotesMenuStates.LoadingState)

    private fun stopAllSync(){
        landSyncJob?.let {
            it.cancel()
            landSyncJob = null
        }
        notesSyncJob?.let {
            it.cancel()
            notesSyncJob = null
        }
    }

    private fun stopNotesSync(){
        notesSyncJob?.let {
            it.cancel()
            notesSyncJob = null
        }
    }

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()
    val isSearchLoading: StateFlow<Boolean> =
        _isSearchLoading.asStateFlow()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<LandNotesMenuStates> = _searchQuery
        .debounce { query ->
            if(query.isNotBlank()) 1500L
            else 0L
        }
        .onEach {
            _isSearchLoading.update { true }
        }
        .combine(_dataState){ query, state ->
            if(state is LandNotesMenuStates.LoadedState && query.isNotBlank())
                state.copy(
                    notes = state.notes.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}\n${it.desc}")
                    }
                )
            else
                state
        }
        .onEach {
            _isSearchLoading.update { false }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            LandNotesMenuStates.LoadingState
        )


    fun startSync(lid: Long){
        stopAllSync()
        landSyncJob = getLandUseCase(lid).onEach {
            it?.let { land ->
                stopNotesSync()
                notesSyncJob = getLandNotesUseCase(lid).onEach { notes ->
                    _dataState.update {
                        LandNotesMenuStates.LoadedState(
                            land = land,
                            notes = notes
                        )
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))
            }?: _dataState.update { LandNotesMenuStates.CantInit }
        }.launchIn(CoroutineScope(Dispatchers.IO))

    }

    fun onSearchChange(query: String){
        if(query.isNotBlank())
            _searchQuery.update {query.trimWithSingleWhitespaces()}
        else
            _searchQuery.update {""}
    }

}