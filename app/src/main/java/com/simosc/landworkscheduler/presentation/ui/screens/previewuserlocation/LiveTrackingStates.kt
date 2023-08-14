package com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation

import android.location.Location
import com.simosc.landworkscheduler.domain.enums.LocationStatues
import com.simosc.landworkscheduler.domain.extension.distanceTo
import com.simosc.landworkscheduler.domain.extension.isInside
import com.simosc.landworkscheduler.domain.extension.toLatLng
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.model.Zone

sealed class LiveTrackingStates {
    data object LoadingState: LiveTrackingStates()

    sealed class ErrorState: LiveTrackingStates(){
        data object NeedPermissionState: ErrorState()
        data object NeedProviderState: ErrorState()
        data object LocationErrorState: ErrorState()

        companion object{
            fun getErrorState(
                locationStatus: LocationStatues
            ): ErrorState {
                return when(locationStatus){
                    LocationStatues.NeedPermission ->
                        NeedPermissionState
                    LocationStatues.NeedProvider ->
                        NeedProviderState
                    else ->
                        LocationErrorState

                }
            }
        }
    }


    sealed class ReadyState(
        val lands: List<Land>,
        val zones: List<Zone>,
        val notes: List<Note>
    ): LiveTrackingStates(){
        class WaitingUserLocationState(
            lands: List<Land> = emptyList(),
            zones: List<Zone> = emptyList(),
            notes: List<Note> = emptyList(),
        ): ReadyState(
            lands = lands,
            zones = zones,
            notes = notes
        ) {
            fun findLocationState(
                userLocation: Location
            ): ReadyState {
                lands.filter{ land ->
                    userLocation.isInside(land.border)
                }.forEach { land ->

                    land.holes.firstOrNull{ hole ->
                        userLocation.isInside(hole)
                    }?:run{

                        notes.filter { note ->
                            note.lid == land.id
                        }.filter { note ->
                            note.center.distanceTo(userLocation.toLatLng()) <= note.radius
                        }.minByOrNull { note ->
                            note.center.distanceTo(userLocation.toLatLng())
                        }?.let{ note ->
                            return InsideNoteState(
                                currentNote = note,
                                lands = lands,
                                zones = zones,
                                notes = notes
                            )
                        }

                        zones.filter{ zone ->
                            zone.lid == land.id
                        }.firstOrNull{ zone ->
                            userLocation.isInside(zone.border)
                        }?.let { zone ->
                            return InsideZoneState(
                                currentZone = zone,
                                lands = lands,
                                zones = zones,
                                notes = notes
                            )
                        }

                        return InsideLandState(
                            currentLand = land,
                            lands = lands,
                            zones = zones,
                            notes = notes
                        )

                    }
                }

                return NotInsideLocationState(
                    lands = lands,
                    zones = zones,
                    notes = notes
                )
            }
        }

        class NotInsideLocationState(
            lands: List<Land> = emptyList(),
            zones: List<Zone> = emptyList(),
            notes: List<Note> = emptyList(),
        ): ReadyState(
            lands = lands,
            zones = zones,
            notes = notes
        )
        class InsideLandState(
            val currentLand: Land,
            lands: List<Land> = emptyList(),
            zones: List<Zone> = emptyList(),
            notes: List<Note> = emptyList(),
        ): ReadyState(
            lands = lands,
            zones = zones,
            notes = notes
        )
        class InsideNoteState(
            val currentNote: Note,
            lands: List<Land> = emptyList(),
            zones: List<Zone> = emptyList(),
            notes: List<Note> = emptyList(),
        ): ReadyState(
            lands = lands,
            zones = zones,
            notes = notes
        )
        class InsideZoneState(
            val currentZone: Zone,
            lands: List<Land> = emptyList(),
            zones: List<Zone> = emptyList(),
            notes: List<Note> = emptyList(),
        ): ReadyState(
            lands = lands,
            zones = zones,
            notes = notes
        )
    }
}