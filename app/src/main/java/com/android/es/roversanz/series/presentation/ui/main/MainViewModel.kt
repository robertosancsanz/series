package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie

class MainViewModel : ViewModel() {

    private val _state = MutableLiveData<MainState>().apply {
        value = MainState.INITIAL
    }

    val state: LiveData<MainState>
        get() = _state


    init {
        _state.postValue(MainState.LIST)
    }

    fun onSerieSelected(serie: Serie) {
        _state.postValue(MainState.DETAIL(serie))
    }

}

