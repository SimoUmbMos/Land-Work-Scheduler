package com.simosc.landworkscheduler.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.simosc.landworkscheduler.presentation.ui.navigation.MainNavHost
import com.simosc.landworkscheduler.presentation.ui.theme.LandWorkSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var keepSplashScreenOpen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepSplashScreenOpen = savedInstanceState?.let {
            if(it.containsKey("KeepSplashScreenOpen")){
                it.getBoolean("KeepSplashScreenOpen")
            }else{
                true
            }
        }?:run{
            true
        }
        installSplashScreen().setKeepOnScreenCondition{
            keepSplashScreenOpen
        }
        setContent {
            LandWorkSchedulerTheme{
                val surface = MaterialTheme.colorScheme.surface
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surface),
                ){
                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = surface,
                            darkIcons = useDarkIcons
                        )
                    }
                    MainNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        onFirstScreenFullyLoaded = {
                            if (keepSplashScreenOpen)
                                keepSplashScreenOpen = false
                        }
                    )
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        keepSplashScreenOpen = if(savedInstanceState.containsKey("KeepSplashScreenOpen")){
            savedInstanceState.getBoolean("KeepSplashScreenOpen")
        }else{
            true
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean(
            "KeepSplashScreenOpen",
            keepSplashScreenOpen
        )
    }
}