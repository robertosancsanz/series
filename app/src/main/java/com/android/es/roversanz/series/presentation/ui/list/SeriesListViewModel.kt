package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class SeriesListViewModel(
        private val useCase: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase,
        private val useCasePauseDownload: PauseDownloadFileUseCase,
        private val useCaseResumeDownload: ResumeDownloadFileUseCase,
        private val useCaseCancelDownload: CancelDownloadFileUseCase) : ViewModel() {

    private val state = MutableLiveData<SeriesListState>().apply {
        value = SeriesListState.INITIAL
    }
    private val downloadState = MutableLiveData<DownloadSerieState>().apply {
        value = DownloadSerieState.INITIAL
    }
    private lateinit var currentSerie: Serie

    init {
        refresh()
    }

    fun refresh() {
        state.postValue(SeriesListState.BUSY)
        useCase.invoke({ onSuccess(it) }, { onError(it) })
    }

    fun onSerieDownload(serie: Serie) {
        currentSerie = serie
        downloadState.postValue(DownloadSerieState.CHECKPERMISSION)
    }

    fun onSerieDownload() {
        useCaseDownload(currentSerie, ::onSuccess, ::onError, ::onQueued, ::onProgress)
    }

    fun onPause(serie: Serie) {
        useCasePauseDownload(serie, ::onPaused)
    }

    fun onResume(serie: Serie) {
        useCaseResumeDownload(serie, ::onResumed)
    }

    fun onRemove(serie: Serie) {
        useCaseCancelDownload(serie, ::onDeleted)
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

    //region Download

    private fun onQueued(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onProgress(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onSuccess(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOADED(serieDownloaded))
    }

    private fun onError(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.ERROR(serieDownloaded))
    }

    private fun onResumed(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onPaused(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onDeleted(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.REMOVE(serieDownloaded))
    }

    //endregion

    fun getState(): LiveData<SeriesListState> = state

    fun getDownloadState(): LiveData<DownloadSerieState> = downloadState

}
