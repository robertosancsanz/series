package com.android.es.roversanz.series

import android.app.Application
import com.android.es.roversanz.series.di.components.DaggerMainComponent
import com.android.es.roversanz.series.di.components.MainComponent
import com.android.es.roversanz.series.di.module.ApplicationModule
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary

class MyApplication : Application() {

    lateinit var component: MainComponent


    override fun onCreate() {
        super.onCreate()

        initializeInjector()

        if (BuildConfig.DEBUG) {
            initializeLeakCanary()

            initializeStetho()
        }
    }

    private fun initializeInjector() {
        component = DaggerMainComponent.builder().applicationModule(ApplicationModule(this)).build()
    }

    private fun initializeLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }

    private fun initializeStetho() {
        Stetho.initializeWithDefaults(this)
    }

}
