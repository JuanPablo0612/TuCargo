package com.juanpablo0612.tucargo

import androidx.compose.ui.window.ComposeUIViewController
import com.juanpablo0612.tucargo.di.initKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import platform.UIKit.UIViewController

private var appInitialized = false

// iOS previously never configured Firebase nor started Koin, so the app
// crashed at the first koinViewModel(). Requires GoogleService-Info.plist in
// the Xcode project (see README).
private fun initAppIfNeeded() {
    if (appInitialized) return
    Firebase.initialize()
    initKoin()
    appInitialized = true
}

fun MainViewController(): UIViewController {
    initAppIfNeeded()
    return ComposeUIViewController { App() }
}
