package com.trynite.chefyarnnn

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class ChefyarnnnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
