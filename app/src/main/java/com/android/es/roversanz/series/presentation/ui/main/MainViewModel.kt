package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val listState = MutableLiveData<MainState>().apply {
        value = MainState.INITIAL
    }

    init {
        listState.postValue(MainState.LIST)
    }

    fun getState(): LiveData<MainState> = listState

}

