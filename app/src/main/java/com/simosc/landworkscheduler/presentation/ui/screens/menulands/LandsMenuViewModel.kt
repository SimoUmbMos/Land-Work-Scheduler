package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultSearchDebounce
import com.simosc.landworkscheduler.domain.extension.tokenizedSearchIn
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.GenerateKml
import com.simosc.landworkscheduler.domain.usecase.land.DeleteLand
import com.simosc.landworkscheduler.domain.usecase.land.GetLands
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuActions.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LandsMenuViewModel @Inject constructor(
    private val getLandsUseCase: GetLands,
    private val getDeleteLandUseCase: DeleteLand,
    private val generateKmlUseCase: GenerateKml
): ViewModel() {
    private var mainJob: Job? = null

    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableSharedFlow<Int?>()

    private val _lands = MutableStateFlow<List<Land>?>(null)
    private val _selectedLands = MutableStateFlow<List<Land>>(emptyList())
    private val _selectedAction = MutableStateFlow(None)

    private val _isLoadingData = MutableStateFlow(false)
    private val _isLoadingAction = MutableStateFlow(false)
    private val _isSearching = MutableStateFlow(false)


    val searchQuery = _searchQuery.asStateFlow()
    val isLoadingData = _isLoadingData.asStateFlow()
    val isLoadingAction = _isLoadingAction.asStateFlow()
    val isSearching = _isSearching.asStateFlow()
    val errorMessage = _error.asSharedFlow()

    private val _currState: StateFlow<LandsMenuStates> =
        combine(_lands,_selectedAction, _selectedLands){ lands, selectedAction, selectedLands ->
            if(lands != null) {
                when (selectedAction) {
                    None -> LandsMenuStates.NormalState(
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
        .onEach {
            _isSearching.update { true }
        }
        .debounce { query ->
            if(query.isNotBlank())
                DefaultSearchDebounce
            else
                0L
        }
        .combine(_currState){ query, currState ->
            when(currState){
                is LandsMenuStates.NormalState -> LandsMenuStates.NormalState(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title}"
                        )
                    }
                )
                is LandsMenuStates.DeleteLands -> LandsMenuStates.DeleteLands(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title}"
                        )
                    },
                    selectedLands = currState.selectedLands.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title}"
                        )
                    }
                )
                is LandsMenuStates.ExportLands -> LandsMenuStates.ExportLands(
                    lands = currState.lands.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title}"
                        )
                    },
                    selectedLands = currState.selectedLands.filter {
                        query.tokenizedSearchIn(
                            "#${it.id} ${it.id}# ${it.title}"
                        )
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


    init{
        startSync()
    }

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

    private fun startSync() {
        _isLoadingData.update { true }
        mainJob = getLandsUseCase()
            .onEach { lands ->
                _lands.update { lands }
                _isLoadingData.update { false }
            }
            .launchIn(
                CoroutineScope(Dispatchers.IO)
            )
    }

    fun refreshLands(){
        stopSync()
        startSync()
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
                        _isLoadingAction.update { false }
                        changeAction(None)
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


    fun onExportSelectedLands(
        context: Context,
        createFileLauncher: ManagedActivityResultLauncher<String, Uri?>,
    ) {
        uiState.value.let { state ->
            if (state is LandsMenuStates.ExportLands && state.selectedLands.isNotEmpty()) {
                createFileLauncher.launch(
                    LocalDateTime.now().run {
                        context.getString( R.string.land_menu_export_file_name,
                            year, monthValue, dayOfMonth, hour, minute, second, nano
                        )
                    }
                )
            } else {
                changeAction(None)
            }
        }
    }

    fun onCreateFileLauncherResult(
        uri: Uri?,
        context: Context
    ) = viewModelScope.launch(Dispatchers.IO){
        uri?.let{

            var fileGenerated = false

            context.contentResolver.openOutputStream(uri,"w")?.use{ outputStream ->
                fileGenerated = generateKml(outputStream)
            }

            if(fileGenerated) {
                context.contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )?.use{ cursor ->
                    val nameIndex = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)

                    launch(Dispatchers.Main){
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.land_menu_message_file_saved,
                                fileName
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            }else{

                uri.path?.let {
                    File(it).let{ file ->
                        file.delete()
                        if(file.exists()){
                            file.canonicalFile.delete()
                            if(file.exists()){
                                context.deleteFile(file.name)
                            }
                        }
                    }
                }

            }

        }?:run{
            changeAction(None)
        }
    }

    private suspend fun generateKml(outputStream: OutputStream): Boolean{
        var result = false
        uiState.value.let{ state ->
            if(state is LandsMenuStates.ExportLands){
                state.selectedLands.let { selectedLands ->
                    _isLoadingAction.update { true }
                    try{
                        if(generateKmlUseCase(selectedLands, outputStream)){
                            result = true
                        }else{
                            _error.tryEmit(R.string.land_menu_error_cant_save_file)
                        }
                    }catch (e: Exception){
                        _error.tryEmit(R.string.land_menu_error_cant_save_file)
                    }
                    _isLoadingAction.update { false }
                }
            }
        }
        changeAction(None)
        return result
    }

}