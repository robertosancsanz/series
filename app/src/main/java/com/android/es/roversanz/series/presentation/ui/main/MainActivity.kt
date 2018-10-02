package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.ActivityScope
import com.android.es.roversanz.series.presentation.ui.list.ListFragment
import com.android.es.roversanz.series.utils.app
import dagger.Component

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inject(app())

        viewModel.getState().observe(this, Observer { state ->
            when (state) {
                MainState.INITIAL -> Log.d(TAG, "Initial State")
                MainState.LIST -> loadFragment(ListFragment.getInstance(), ListFragment::class.java.simpleName, false)
                is MainState.DETAIL -> Log.d(TAG, "Load Detail")
                else -> Log.d(TAG, "Something is wrong")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.getState().removeObservers(this)
    }

    private fun loadFragment(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val fragmentFounded = supportFragmentManager.findFragmentByTag(tag)
        if (fragmentFounded == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, fragment, tag)
                if (addToBackStack) {
                    addToBackStack(tag)
                }
            }.commit()
        }
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
