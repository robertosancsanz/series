package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class SeriesListViewModel(
        private val useCase: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase) : ViewModel() {

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
        useCaseDownload.invoke(
                serie,
                ::onSuccess,
                ::onErrorDownload,
                ::onQueued
        )
    }

    //region GetSeriesListUseCase

    private fun onSuccess(list: List<Serie>) = if (list.isEmpty()) {
        state.postValue(SeriesListState.EMPTY)
    } else {
        state.postValue(SeriesListState.DONE(list))
    }

    private fun onError(message: String) {
        state.postValue(SeriesListState.ERROR(message))
    }

    //endregion

    //region DownloadFileUseCase
    private fun onQueued(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onSuccess(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOADED(serieDownloaded))
    }

    private fun onErrorDownload(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.ERROR(serieDownloaded))
    }

    //endregion

    fun getState(): LiveData<SeriesListState> = state

    fun getDownloadState(): LiveData<DownloadSerieState> = downloadState

}
