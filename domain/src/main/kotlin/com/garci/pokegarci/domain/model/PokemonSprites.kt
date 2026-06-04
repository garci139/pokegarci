package com.garci.pokegarci.domain.model

fun Pokemon.hasShinySprites(): Boolean =
    frontShinyImageUrl.isNotBlank() && backShinyImageUrl.isNotBlank()

fun Pokemon.spriteUrl(showingBack: Boolean, shiny: Boolean): String {
    return when {
        showingBack && shiny -> backShinyImageUrl
        showingBack -> backImageUrl
        shiny -> frontShinyImageUrl
        else -> imageUrl
    }
}