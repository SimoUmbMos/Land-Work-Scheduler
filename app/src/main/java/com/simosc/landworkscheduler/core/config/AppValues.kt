package com.simosc.landworkscheduler.core.config

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

val DefaultDialogColor = Color(0xFFD32F2F)
val DefaultDialogColors = listOf(
    Color(0xFFD32F2F),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722)
)

val DefaultLandColor = Color(0xFFCDDC39)
val DefaultZoneColor = Color(0xFF009688)
val DefaultNoteColor = Color(0xFF3F51B5)

val DefaultMapTarget = LatLng(0.0, 0.0)
const val DefaultMapZoom = 13f

const val DefaultMapItemStrokeAlpha = 1f
const val DefaultMapItemFillAlpha = .5f
const val DefaultSelectedPointFillAlpha = .6f
const val DefaultUnselectedPointFillAlpha = .2f

const val DefaultNavHostAnimationDurationMillis = 100

const val DefaultSearchDebounce = 1500L