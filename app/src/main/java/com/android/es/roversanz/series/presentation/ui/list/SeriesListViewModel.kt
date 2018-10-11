package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase.DownloadFileUseCaseListener
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class SeriesListViewModel(
        private val useCase: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase) : ViewModel(), DownloadFileUseCaseListener {

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
        useCaseDownload.invoke(currentSerie, this)
    }

    fun onPause(serie: Serie) {
        useCaseDownload.invokePaused(serie, this)
    }

    fun onResume(serie: Serie) {
        useCaseDownload.invokeResume(serie, this)
    }

    fun onRemove(serie: Serie) {
        useCaseDownload.invokeCancel(serie, this)
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

    override fun onQueued(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    override fun onProgress(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    override fun onPaused(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    override fun onResumed(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOAD(serieDownloaded))
    }

    override fun onSuccess(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.DOWNLOADED(serieDownloaded))
    }

    override fun onError(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.ERROR(serieDownloaded))
    }

    override fun onDeleted(serieDownloaded: SerieDownloaded) {
        downloadState.postValue(DownloadSerieState.REMOVE(serieDownloaded))
    }

    //endregion

    fun getState(): LiveData<SeriesListState> = state

    fun getDownloadState(): LiveData<DownloadSerieState> = downloadState

}
