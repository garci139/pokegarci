package com.garci.pokegarci

import android.os.Bundle
import android.view.MotionEvent
import androidx.navigation.fragment.NavHostFragment
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.GuessKeyboardDismiss
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseLocaleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (isGuessScreenVisible())
            GuessKeyboardDismiss.handle(this, event)
        return super.dispatchTouchEvent(event)
    }

    private fun isGuessScreenVisible(): Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return false
        return navHost.navController.currentDestination?.id == R.id.guessFragment
    }
}