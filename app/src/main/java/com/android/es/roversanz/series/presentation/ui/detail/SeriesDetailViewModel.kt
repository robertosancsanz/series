package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.toPercentage

class SeriesDetailViewModel(private val useCase: DownloadFileUseCase,
                            private val serie: Serie) : ViewModel() {

    private val state = MutableLiveData<SeriesDetailState>().apply {
        value = SeriesDetailState.INITIAL
    }

    init {
        state.postValue(SeriesDetailState.DONE(serie))
    }

    fun downloadChapter() = when (state.value) {
        SeriesDetailState.PAUSED         -> useCase.invokeResume(serie)
        is SeriesDetailState.DOWNLOADING -> useCase.invokePaused(serie)
        is SeriesDetailState.DOWNLOADED  -> state.postValue(DOWNLOADED(serie.file))
        else                             -> {
            useCase.invoke(
                    serie,
                    onSuccess = { onSuccess(it) },
                    onError = { onError(it) },
                    onQueued = { onQueued() },
                    onProgress = { onProgress(it) },
                    onPaused = { onPaused() },
                    onResumed = { onResumed(it) },
                    onDeleted = { onDeleted() }

            )
        }
    }


    fun cancelDownloadChapter() {
        useCase.invokeCancel(serie)
    }

    private fun onProgress(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    private fun onSuccess(serieDownloaded: SerieDownloaded) {
        this.serie.file = serieDownloaded.filePath
        state.postValue(SeriesDetailState.DOWNLOADED(null))
    }

    private fun onQueued() {
        state.postValue(SeriesDetailState.DOWNLOADING(0.toPercentage()))
    }

    private fun onPaused() {
        state.postValue(SeriesDetailState.PAUSED)
    }

    private fun onResumed(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    private fun onDeleted() {
        state.postValue(SeriesDetailState.INITIAL)
    }

    private fun onError(serieDownloaded: SerieDownloaded) {
        serieDownloaded.error?.let { state.postValue(SeriesDetailState.ERROR(it)) }
    }

    fun getState(): LiveData<SeriesDetailState> = state

}
