package com.android.es.roversanz.series.presentation.ui.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.ActivityScope
import com.android.es.roversanz.series.utils.app
import dagger.Component

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inject(app())

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
