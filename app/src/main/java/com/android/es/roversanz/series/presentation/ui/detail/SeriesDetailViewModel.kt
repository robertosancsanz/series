package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.COMPLETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.DELETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.ERROR
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PAUSED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PROGRESS
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.QUEUED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.RESUMED
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.CHECKPERMISSION
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.toStringPercentage

class SeriesDetailViewModel(private val useCaseDownload: DownloadFileUseCase,
                            private val useCasePauseDownload: PauseDownloadFileUseCase,
                            private val useCaseResumeDownload: ResumeDownloadFileUseCase,
                            private val useCaseCancelDownload: CancelDownloadFileUseCase,
                            private val serie: Serie) : ViewModel() {

    private val _state = MutableLiveData<SeriesDetailState>().apply {
        value = SeriesDetailState.INITIAL
    }

    val state: LiveData<SeriesDetailState>
        get() = _state

    private val observer: Observer<DownloadManagerState> = Observer { state ->
        when (state) {
            is QUEUED    -> onQueued(state.serieDownloaded)
            is PROGRESS  -> onProgress(state.serieDownloaded)
            is COMPLETED -> onSuccess(state.serieDownloaded)
            is RESUMED   -> onResumed(state.serieDownloaded)
            is PAUSED    -> onPaused(state.serieDownloaded)
            is ERROR     -> onError(state.serieDownloaded)
            is DELETED   -> onDeleted(state.serieDownloaded)
        }
    }

    init {
        useCaseDownload.state.observeForever(observer)
        _state.postValue(SeriesDetailState.DONE(serie))
    }

    override fun onCleared() {
        super.onCleared()
        useCaseDownload.state.removeObserver(observer)
    }

    fun downloadChapter() = when (_state.value) {
        SeriesDetailState.PAUSED          -> useCaseResumeDownload(serie)
        is SeriesDetailState.DOWNLOADING  -> useCasePauseDownload(serie)
        is SeriesDetailState.DOWNLOADED   -> _state.postValue(DOWNLOADED(serie.file))
        SeriesDetailState.CHECKPERMISSION -> useCaseDownload(serie)
        else                              -> _state.postValue(CHECKPERMISSION)
    }


    fun cancelDownloadChapter() {
        useCaseCancelDownload(serie)
    }

    //region Download


    private fun onQueued(serieDownloaded: SerieDownloaded) {
        _state.postValue(SeriesDetailState.DOWNLOADING(0.toStringPercentage()))
    }

    private fun onProgress(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { _state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    private fun onSuccess(serieDownloaded: SerieDownloaded) {
        this.serie.file = serieDownloaded.filePath
        _state.postValue(SeriesDetailState.DOWNLOADED(null))
    }

    private fun onError(serieDownloaded: SerieDownloaded) {
        serieDownloaded.error?.let { _state.postValue(SeriesDetailState.ERROR(it)) }
    }

    private fun onPaused(serieDownloaded: SerieDownloaded) {
        _state.postValue(SeriesDetailState.PAUSED)
    }

    private fun onResumed(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { _state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    private fun onDeleted(serieDownloaded: SerieDownloaded) {
        _state.postValue(SeriesDetailState.INITIAL)
    }

    //endregion

}
