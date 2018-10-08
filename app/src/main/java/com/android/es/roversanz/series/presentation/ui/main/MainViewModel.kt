package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie

class MainViewModel : ViewModel() {

    private val listState = MutableLiveData<MainState>().apply {
        value = MainState.INITIAL
    }

    init {
        listState.postValue(MainState.LIST)
    }

    fun onSerieSelected(serie: Serie) {
        listState.postValue(MainState.DETAIL(serie))
    }

    fun getState(): LiveData<MainState> = listState

}

