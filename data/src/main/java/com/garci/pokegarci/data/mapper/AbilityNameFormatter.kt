package com.garci.pokegarci.data.mapper

object AbilityNameFormatter {

    fun format(originalName: String): String {
        return originalName
            .split("-")
            .joinToString(" ") { word ->
                word.replaceFirstChar { char -> char.uppercase() }
            }
    }
}
