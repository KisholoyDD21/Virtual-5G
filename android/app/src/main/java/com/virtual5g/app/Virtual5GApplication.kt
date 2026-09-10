package com.virtual5g.app

import android.app.Application
import com.virtual5g.app.di.AppContainer
import com.virtual5g.data.repository.NetworkSamplingWorker

class Virtual5GApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
        NetworkSamplingWorker.schedule(applicationContext)
    }
}
