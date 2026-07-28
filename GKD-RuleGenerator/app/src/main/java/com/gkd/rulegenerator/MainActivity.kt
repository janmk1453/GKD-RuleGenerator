// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.gkd.rulegenerator.ui.MainScreen
import com.gkd.rulegenerator.ui.theme.GKDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dispatcherOwner = rememberNavigationEventDispatcherOwner(parent = null)
            androidx.compose.runtime.CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides dispatcherOwner
            ) {
                GKDTheme {
                    MainScreen()
                }
            }
        }
    }
}
