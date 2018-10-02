package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class FactoryMainViewModel : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel() as T
                ?: throw IllegalArgumentException("This factory can only create MainViewModel instances")
    }

}
