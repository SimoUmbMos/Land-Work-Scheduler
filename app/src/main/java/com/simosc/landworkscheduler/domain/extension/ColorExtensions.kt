package com.simosc.landworkscheduler.domain.extension

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Color.invert() = copy(
    red = 1.0f - red,
    green = 1.0f - green,
    blue = 1.0f - blue,
)

fun Color.toArgbString(): String =
    Integer.toHexString(toArgb()).lowercase()

fun Color.Companion.parseColor(color: String): Color =
    when(color.length){
        8 -> color.lowercase()
        6 -> "ff$color".lowercase()
        3 -> "ff${color[0]}${color[0]}${color[1]}${color[1]}${color[2]}${color[2]}".lowercase()
        else -> throw IllegalArgumentException()
    }.let{
        Color(it.toLong(16).toInt())
    }