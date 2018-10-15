package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.CHECKPERMISSION
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.toPercentage

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


    init {
        _state.postValue(SeriesDetailState.DONE(serie))
    }

    fun downloadChapter() = when (_state.value) {
        SeriesDetailState.PAUSED          -> useCaseResumeDownload(serie, ::onResumed)
        is SeriesDetailState.DOWNLOADING  -> useCasePauseDownload(serie, ::onPaused)
        is SeriesDetailState.DOWNLOADED   -> _state.postValue(DOWNLOADED(serie.file))
        SeriesDetailState.CHECKPERMISSION -> useCaseDownload(serie, ::onSuccess, ::onError, ::onQueued, ::onProgress)
        else                              -> _state.postValue(CHECKPERMISSION)
    }


    fun cancelDownloadChapter() {
        useCaseCancelDownload(serie, ::onDeleted)
    }

    //region Download


    private fun onQueued(serieDownloaded: SerieDownloaded) {
        _state.postValue(SeriesDetailState.DOWNLOADING(0.toPercentage()))
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
