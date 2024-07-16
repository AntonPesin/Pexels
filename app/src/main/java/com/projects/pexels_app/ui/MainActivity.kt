package com.projects.pexels_app.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.BuildCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.internal.GsonBuildConfig
import com.projects.pexels_app.R
import com.projects.pexels_app.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoadingCollections.value

        }

        lifecycleScope.launch {
            mainViewModel.loadCollections()
        }
    }


}