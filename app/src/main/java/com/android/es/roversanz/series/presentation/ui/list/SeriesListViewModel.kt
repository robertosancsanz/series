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
        private val useCaseGetSeries: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase,
        private val useCasePauseDownload: PauseDownloadFileUseCase,
        private val useCaseResumeDownload: ResumeDownloadFileUseCase,
        private val useCaseCancelDownload: CancelDownloadFileUseCase) : ViewModel() {

    private val _state = MutableLiveData<SeriesListState>().apply {
        value = SeriesListState.INITIAL
    }
    val state: LiveData<SeriesListState>
        get() = _state

    private val _downloadState = MutableLiveData<DownloadSerieState>().apply {
        value = DownloadSerieState.INITIAL
    }
    val downloadState: LiveData<DownloadSerieState>
        get() = _downloadState

    private lateinit var currentSerie: Serie

    init {
        refresh()
    }

    fun refresh() {
        _state.postValue(SeriesListState.BUSY)
        useCaseGetSeries({ onSuccess(it) }, { onError(it) })
    }

    fun onSerieDownload(serie: Serie) {
        currentSerie = serie
        _downloadState.postValue(DownloadSerieState.CHECKPERMISSION)
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
        _state.postValue(SeriesListState.EMPTY)
    } else {
        _state.postValue(SeriesListState.DONE(list))
    }

    private fun onError(message: String) {
        _state.postValue(SeriesListState.ERROR(message))
    }

    //endregion

    //region Download

    private fun onQueued(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onProgress(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onSuccess(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.DOWNLOADED(serieDownloaded))
    }

    private fun onError(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.ERROR(serieDownloaded))
    }

    private fun onResumed(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onPaused(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    private fun onDeleted(serieDownloaded: SerieDownloaded) {
        _downloadState.postValue(DownloadSerieState.REMOVE(serieDownloaded))
    }

    //endregion

}
