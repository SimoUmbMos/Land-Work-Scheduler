package com.simosc.landworkscheduler.domain.extension

fun String.tokenizedSearchIn(text: String): Boolean{
    this.trimWithSingleWhitespaces()
        .split(" ")
        .forEach{ word ->
            if(!text.contains(word, true))
                return false
        }
    return true
}

fun String.trimWithSingleWhitespaces() =
    this.trim()
    .replace("[\\t\\n\\r]+".toRegex(), " ")
    .replace("\\s+".toRegex(), " ")