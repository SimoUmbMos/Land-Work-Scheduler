package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.core.config.DefaultSearchDebounce
import com.simosc.landworkscheduler.domain.extension.tokenizedSearchIn
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.GetKmlText
import com.simosc.landworkscheduler.domain.usecase.land.DeleteLand
import com.simosc.landworkscheduler.domain.usecase.land.GetLands
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuActions.*
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LandsMenuViewModel @Inject constructor(
    private val getLandsUseCase: GetLands,
    private val getDeleteLandUseCase: DeleteLand,
    private val getKmlTextUseCase: GetKmlText
): ViewModel() {
    private var mainJob: Job? = null

    private val _searchQuery: MutableStateFlow<String> =
        MutableStateFlow("")

    private val _lands: MutableStateFlow<List<Land>?> =
        MutableStateFlow(null)
    private val _selectedLands: MutableStateFlow<List<Land>> =
        MutableStateFlow(emptyList())
    private val _selectedAction: MutableStateFlow<LandsMenuActions> =
        MutableStateFlow(None)

    private val _isLoadingAction:MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val _isSearching:MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    private val _currState: StateFlow<LandsMenuStates> =
        combine(_lands,_selectedAction, _selectedLands){ lands, selectedAction, selectedLands ->
            if(lands != null) {
                when (selectedAction) {
                    None -> LandsMenuStates.Loaded(
                        lands = lands
                    )

                    Export -> LandsMenuStates.ExportLands(
                        lands = lands,
                        selectedLands = selectedLands
                    )

                    Delete -> LandsMenuStates.DeleteLands(
                        lands = lands,
                        selectedLands = selectedLands
                    )
                }
            } else {
                LandsMenuStates.Loading
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            LandsMenuStates.Loading
        )

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<LandsMenuStates> = _searchQuery
        .debounce { query ->
            if(query.isNotBlank())
                DefaultSearchDebounce
            else
                0L
        }
        .onEach {
            _isSearching.update { true }
        }
        .combine(_currState){ query, currState ->
            when(currState){
                is LandsMenuStates.Loaded -> LandsMenuStates.Loaded(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}")
                    }
                )
                is LandsMenuStates.DeleteLands -> LandsMenuStates.DeleteLands(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}")
                    },
                    selectedLands = currState.selectedLands.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}")
                    }
                )
                is LandsMenuStates.ExportLands -> LandsMenuStates.ExportLands(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}")
                    },
                    selectedLands = currState.selectedLands.filter {
                        query.tokenizedSearchIn("#${it.id} ${it.title}")
                    }
                )
                else ->
                    currState
            }
        }
        .onEach {
            _isSearching.update { false }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            LandsMenuStates.Loading
        )

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()
    val isLoadingAction:StateFlow<Boolean> =
        _isLoadingAction.asStateFlow()
    val isSearching:StateFlow<Boolean> =
        _isSearching.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        stopSync()
    }

    private fun stopSync(){
        mainJob?.let{
            it.cancel()
            mainJob = null
        }
    }

    fun loadLands() {
        stopSync()
        mainJob = getLandsUseCase().onEach { lands ->
            _lands.update { lands }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun toggleLand(land: Land) {
        _selectedLands.update {
            it.toMutableList().apply{
                if(contains(land))
                    remove(land)
                else
                    add(land)
            }.toList()
        }
    }

    fun createFileToExport(createFileLauncher: ManagedActivityResultLauncher<String, Uri?>) {
        uiState.value.let { state ->
            if(state is LandsMenuStates.ExportLands) {
                if(state.selectedLands.isNotEmpty()) {
                    LocalDateTime.now().run {
                        createFileLauncher.launch(
                            "exported_lands_" +
                                    "${year}${monthValue}${dayOfMonth}_" +
                                    "${hour}${minute}${second}_${nano}"
                        )
                    }
                }else{
                    changeAction(None)
                }
            }
        }
    }

    fun getLandsKmlString(): String {
        var kmlText = ""
        uiState.value.let{ state ->
            if(state is LandsMenuStates.ExportLands){
                state.selectedLands.let { selectedLands ->
                    _isLoadingAction.update { true }
                    kmlText = getKmlTextUseCase(selectedLands)
                    _isLoadingAction.update { false }
                }
            }
        }
        return kmlText
    }

    fun executeLandsDelete() {
        uiState.value.let { state ->
            if(state is LandsMenuStates.DeleteLands) {
                state.selectedLands.let{ selectedLands ->
                    _isLoadingAction.update { true }
                    viewModelScope.launch(Dispatchers.IO){
                        selectedLands.forEach {
                            getDeleteLandUseCase(it)
                        }
                        _lands.update {
                            it?.toMutableList()?.apply {
                                removeAll(selectedLands)
                            }?.toList()
                        }
                        changeAction(None)
                        _isLoadingAction.update { false }
                    }
                }
            }
        }
    }

    fun changeAction(action: LandsMenuActions) {
        _selectedLands.update { emptyList() }
        _selectedAction.update { action }
    }

    fun onSearchChange(searchQuery: String) {
        searchQuery.replace("\\s+".toRegex()," ").let{ query ->
            if(query.isNotBlank())
                _searchQuery.update { query }
            else
                _searchQuery.update { "" }
        }
    }

}