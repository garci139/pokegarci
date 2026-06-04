package com.garci.pokegarci.util

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.garci.pokegarci.R

object NavAnimations {

    fun options(builder: NavOptions.Builder.() -> Unit = {}): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.nav_slide_in_right)
            .setExitAnim(R.anim.nav_slide_out_left)
            .setPopEnterAnim(R.anim.nav_slide_in_left)
            .setPopExitAnim(R.anim.nav_slide_out_right)
            .apply(builder)
            .build()
    }

    fun NavController.navigateWithSlide(@IdRes resId: Int) {
        navigate(resId, null, options())
    }

    fun NavController.navigateWithSlide(
        @IdRes resId: Int,
        builder: NavOptions.Builder.() -> Unit,
    ) {
        navigate(resId, null, options(builder))
    }
}
