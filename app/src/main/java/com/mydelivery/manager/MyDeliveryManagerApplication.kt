package com.mydelivery.manager

import android.app.Application
import com.mydelivery.manager.data.AppContainer

class MyDeliveryManagerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // The database itself is opened lazily on first use, so app start-up stays fast.
        container = AppContainer(this)
    }
}
