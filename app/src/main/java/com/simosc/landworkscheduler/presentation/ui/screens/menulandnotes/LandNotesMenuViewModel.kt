package com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.core.config.DefaultSearchDebounce
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
        stopSync()
    }

    private var syncJob: Job? = null

    private val _landId: MutableStateFlow<Long> =
        MutableStateFlow(0L)
    private val _data: MutableStateFlow<LandNotesMenuStates> =
        MutableStateFlow(LandNotesMenuStates.LoadingState)
    private val _searchQuery: MutableStateFlow<String> =
        MutableStateFlow("")
    private val _isSearching: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()
    val isSearching: StateFlow<Boolean> =
        _isSearching.asStateFlow()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<LandNotesMenuStates> = _searchQuery
        .onEach {
            _isSearching.update { true }
        }
        .debounce { query ->
            if(query.isNotBlank())
                DefaultSearchDebounce
            else
                0L
        }
        .combine(_data){ query, data ->
            if(data is LandNotesMenuStates.LoadedState){
                LandNotesMenuStates.LoadedState(
                    land = data.land,
                    notes = data.notes.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title} ${it.desc}"
                        )
                    }
                )
            }else{
                data
            }
        }
        .onEach {
            _isSearching.update { false }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            LandNotesMenuStates.LoadingState
        )


    private fun stopSync(){
        syncJob?.let {
            it.cancel()
            syncJob = null
        }
    }

    private fun startSync(){
        stopSync()
        _landId.value.let{ lid ->
            syncJob = combine(
                getLandUseCase(lid),
                getLandNotesUseCase(lid)
            ){ land, notes ->
                land?.let {
                    _data.update {
                        LandNotesMenuStates.LoadedState(
                            land = land.copy(),
                            notes = notes.toList()
                        )
                    }
                }?:run{
                    _data.update {
                        LandNotesMenuStates.CantInit
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
        }
    }

    fun setLandId(lid: Long){
        _landId.update { if(lid > 0L) lid else 0L }
        startSync()
    }

    fun onSearchQueryUpdate(newQuery: String){
        _searchQuery.update { newQuery.trimWithSingleWhitespaces().ifBlank { "" } }
    }
}