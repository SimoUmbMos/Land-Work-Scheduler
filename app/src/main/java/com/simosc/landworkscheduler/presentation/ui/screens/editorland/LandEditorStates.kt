package com.simosc.landworkscheduler.presentation.ui.screens.editorland

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.domain.extension.distanceTo
import com.simosc.landworkscheduler.domain.model.Land
import kotlin.math.absoluteValue

sealed class LandEditorStates{

    data object LoadingState: LandEditorStates()

    class NeedLocation(
        val land: Land
    ): LandEditorStates(){
        fun toNormalState(title: String): NormalState{
            return NormalState(land = land.copy(title = title))
        }
    }

    class NormalState(
        val land: Land,
        val newTitle: String = land.title,
        val newColor: Color = land.color.copy(),
        val newBorder: List<LatLng> = land.border.toList(),
        val newHoles: List<List<LatLng>> = land.holes.toList(),
    ): LandEditorStates(){
        fun needSave(): Boolean{
            return land != land.copy(
                title = newTitle,
                color = newColor,
                border = newBorder,
                holes = newHoles,
            )
        }

        fun toEditState(action: LandEditorActions): LandEditorStates{
            return when(action){
                LandEditorActions.ADD_POINTS -> AddPointState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
                LandEditorActions.ADD_BETWEEN_POINTS -> AddBetweenPointState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
                LandEditorActions.DELETE_POINTS -> DeletePointState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
                LandEditorActions.EDIT_POINTS -> EditPointState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
                LandEditorActions.CHANGE_TITLE -> EditTitleState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
                LandEditorActions.CHANGE_COLOR -> EditColorState(
                    land = land,
                    newTitle = newTitle,
                    newColor = newColor,
                    newBorder = newBorder,
                    newHoles = newHoles,
                )
            }
        }
    }

    sealed class EditState(
        val land: Land,
        val newTitle: String = land.title,
        val newColor: Color = land.color.copy(),
        val newBorder: List<LatLng> = land.border.toList(),
        val newHoles: List<List<LatLng>> = land.holes.toList(),
    ): LandEditorStates(){
        abstract fun submitEdit(): NormalState
        abstract fun cancelEdit(): NormalState
    }

    class AddPointState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempBorder: List<LatLng> = newBorder.toList(),
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(point: LatLng): EditState {
            return AddPointState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
                tempBorder = tempBorder.toMutableList().apply{
                    add(point)
                }.toList(),
            )
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = tempBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }

    class AddBetweenPointState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempBorder: List<LatLng> = newBorder.toList(),
        val startIndex: Int = -1,
        val endIndex: Int = -1,
        val selectedIndex: Int = -1,
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(point: LatLng): EditState {
            if(selectedIndex == -1) {
                if (startIndex == -1) {
                    tempBorder.minByOrNull { it.distanceTo(point) }?.let {
                        return AddBetweenPointState(
                            land = land,
                            newTitle = newTitle,
                            newColor = newColor,
                            newBorder = newBorder,
                            newHoles = newHoles,
                            tempBorder = tempBorder,
                            startIndex = tempBorder.indexOf(it),
                            endIndex = -1,
                            selectedIndex = -1,
                        )
                    }
                } else if (endIndex == -1) {
                    tempBorder.minByOrNull { it.distanceTo(point) }?.let {
                        val tempIndex = tempBorder.indexOf(it)
                        val lastIndex = tempBorder.size - 1
                        if (tempIndex == lastIndex && startIndex == 0) {
                            return AddBetweenPointState(
                                land = land,
                                newTitle = newTitle,
                                newColor = newColor,
                                newBorder = newBorder,
                                newHoles = newHoles,
                                tempBorder = tempBorder,
                                startIndex = startIndex,
                                endIndex = tempIndex,
                                selectedIndex = -1,
                            )
                        } else if (startIndex == lastIndex && tempIndex == 0) {
                            return AddBetweenPointState(
                                land = land,
                                newTitle = newTitle,
                                newColor = newColor,
                                newBorder = newBorder,
                                newHoles = newHoles,
                                tempBorder = tempBorder,
                                startIndex = tempIndex,
                                endIndex = startIndex,
                                selectedIndex = -1,
                            )
                        } else if (startIndex.minus(tempIndex).absoluteValue == 1) {
                            return AddBetweenPointState(
                                land = land,
                                newTitle = newTitle,
                                newColor = newColor,
                                newBorder = newBorder,
                                newHoles = newHoles,
                                tempBorder = tempBorder,
                                startIndex = if (tempIndex > startIndex) startIndex else tempIndex,
                                endIndex = if (tempIndex > startIndex) tempIndex else startIndex,
                                selectedIndex = -1,
                            )
                        }
                    }
                } else {
                    return AddBetweenPointState(
                        land = land,
                        newTitle = newTitle,
                        newColor = newColor,
                        newBorder = newBorder,
                        newHoles = newHoles,
                        tempBorder = tempBorder.toMutableList().apply {
                            if(startIndex == 0 && endIndex == tempBorder.lastIndex) {
                                add(point)
                            } else{
                                add(endIndex, point)
                            }
                        }.toList(),
                        startIndex = startIndex,
                        endIndex = if(startIndex == 0 && endIndex == tempBorder.lastIndex)
                            endIndex
                        else
                            endIndex + 1,
                        selectedIndex = if(startIndex == 0 && endIndex == tempBorder.lastIndex)
                            endIndex + 1
                        else
                            endIndex,
                    )
                }
            } else {
                tempBorder.toMutableList().let {
                    it[selectedIndex] = point
                    return AddBetweenPointState(
                        land = land,
                        newTitle = newTitle,
                        newColor = newColor,
                        newBorder = newBorder,
                        newHoles = newHoles,
                        tempBorder = it,
                        startIndex = startIndex,
                        endIndex = endIndex,
                        selectedIndex = selectedIndex,
                    )
                }
            }
            return this
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = tempBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }

    class DeletePointState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempBorder: List<LatLng> = newBorder.toList(),
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(point: LatLng): EditState {
            if(tempBorder.isNotEmpty())
                tempBorder.minBy {
                    it.distanceTo(point)
                }.let { min ->
                    return DeletePointState(
                        land = land,
                        newTitle = newTitle,
                        newColor = newColor,
                        newBorder = newBorder,
                        newHoles = newHoles,
                        tempBorder = tempBorder.toMutableList().apply{
                            remove(min)
                        }.toList(),
                    )
                }
            return this
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = tempBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }

    class EditPointState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempBorder: List<LatLng> = newBorder.toList(),
        val selectedIndex: Int = -1
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(point: LatLng): EditState {
            if(selectedIndex > -1 && selectedIndex < tempBorder.size){
                tempBorder.toMutableList().let{
                    it[selectedIndex] = point
                    return EditPointState(
                        land = land,
                        newTitle = newTitle,
                        newColor = newColor,
                        newBorder = newBorder,
                        newHoles = newHoles,
                        tempBorder = it.toList(),
                        selectedIndex = -1
                    )
                }
            }else if(tempBorder.isNotEmpty()){
                tempBorder.minBy {
                    it.distanceTo(point)
                }.let { min ->
                    return EditPointState(
                        land = land,
                        newTitle = newTitle,
                        newColor = newColor,
                        newBorder = newBorder,
                        newHoles = newHoles,
                        tempBorder = tempBorder,
                        selectedIndex = tempBorder.indexOf(min)
                    )
                }
            }
            return this
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = tempBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }

    class EditTitleState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempTitle: String = newTitle,
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(title: String): EditState{
            return EditTitleState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
                tempTitle = title
            )
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = tempTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }

    class EditColorState(
        land: Land,
        newTitle: String = land.title,
        newColor: Color = land.color.copy(),
        newBorder: List<LatLng> = land.border.toList(),
        newHoles: List<List<LatLng>> = land.holes.toList(),
        val tempColor: Color = newColor.copy(),
    ): EditState(
        land = land,
        newTitle = newTitle,
        newColor = newColor,
        newBorder = newBorder,
        newHoles = newHoles,
    ) {
        fun performAction(color: Color): EditState{
            return EditColorState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
                tempColor = color
            )
        }

        override fun submitEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = tempColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }

        override fun cancelEdit(): NormalState {
            return NormalState(
                land = land,
                newTitle = newTitle,
                newColor = newColor,
                newBorder = newBorder,
                newHoles = newHoles,
            )
        }
    }
}