package com.garci.pokegarci.presentation.firstmenu

sealed interface FirstMenuLoadState {
    data object Idle : FirstMenuLoadState
    data object Loading : FirstMenuLoadState
    data object Ready : FirstMenuLoadState
    data object Error : FirstMenuLoadState
}
