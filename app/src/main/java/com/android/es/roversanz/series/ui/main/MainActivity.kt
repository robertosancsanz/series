package com.android.es.roversanz.series.ui.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.es.roversanz.series.MyApplication
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.di.components.MainComponent
import com.android.es.roversanz.series.di.scopes.ActivityScope
import com.android.es.roversanz.series.utils.ResourceProvider
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Component
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var resourceProvider: ResourceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inject(app())

        logger.d(MainActivity::class.java.simpleName, "Creating: ${resourceProvider.getString(R.string.app_name)}")
    }


    private fun inject(app: MyApplication) {
        DaggerMainActivity_MainActivityComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }


    @ActivityScope
    @Component(dependencies = [(MainComponent::class)])
    internal interface MainActivityComponent {

        fun inject(activity: MainActivity)
    }

}
