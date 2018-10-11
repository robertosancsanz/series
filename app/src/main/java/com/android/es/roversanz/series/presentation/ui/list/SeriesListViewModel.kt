package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class SeriesListViewModel(private val useCase: GetSeriesListUseCase) : ViewModel() {

    private val state = MutableLiveData<SeriesListState>().apply {
        value = SeriesListState.INITIAL
    }

    private val downloadState = MutableLiveData<DownloadSerieState>().apply {
        value = DownloadSerieState.INITIAL
    }

    init {
        refresh()
    }

    fun refresh() {
        state.postValue(SeriesListState.BUSY)
        useCase.invoke({ onSuccess(it) }, { onError(it) })
    }

    fun onSerieDownload(serie: Serie) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serie))
    }

    private fun onSuccess(list: List<Serie>) = if (list.isEmpty()) {
        state.postValue(SeriesListState.EMPTY)
    } else {
        state.postValue(SeriesListState.DONE(list))
    }

    private fun onError(message: String) {
        state.postValue(SeriesListState.ERROR(message))
    }

    fun getState(): LiveData<SeriesListState> = state

    fun getDownloadState(): LiveData<DownloadSerieState> = downloadState

}
