package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class SeriesListViewModel(private val useCase: GetSeriesListUseCase) : ViewModel() {

    private val state = MutableLiveData<SeriesListState>().apply {
        value = SeriesListState.INITIAL
    }

    init {
        state.postValue(SeriesListState.BUSY)
        useCase.invoke({
            state.postValue(SeriesListState.DONE(it))
        }, {
            state.postValue(SeriesListState.ERROR(it))
        })
    }

    fun getState() = state
}
