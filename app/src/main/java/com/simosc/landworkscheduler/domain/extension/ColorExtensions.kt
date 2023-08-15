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
        4 -> "${color[0]}${color[0]}${color[1]}${color[1]}${color[2]}${color[2]}${color[3]}${color[3]}".lowercase()
        3 -> "ff${color[0]}${color[0]}${color[1]}${color[1]}${color[2]}${color[2]}".lowercase()
        else -> throw IllegalArgumentException()
    }.let{
        Color(it.toLong(16).toInt())
    }


fun Color.toAbgrString(): String = toArgbString().let{
    "${it[0]}${it[1]}${it[6]}${it[7]}${it[4]}${it[5]}${it[2]}${it[3]}"
}

fun Color.Companion.parseColorAbgr(color: String): Color = when(color.length){
    8 -> "${color[0]}${color[1]}${color[6]}${color[7]}${color[4]}${color[5]}${color[2]}${color[3]}"
    6 -> "${color[4]}${color[5]}${color[2]}${color[3]}${color[0]}${color[1]}"
    4 -> "${color[0]}${color[3]}${color[2]}${color[1]}"
    3 -> "${color[2]}${color[1]}${color[0]}"
    else -> color
}.let{
    Color.parseColor(it)
}