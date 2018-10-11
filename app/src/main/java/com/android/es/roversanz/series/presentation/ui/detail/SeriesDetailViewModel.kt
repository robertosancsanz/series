package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.utils.toPercentage
import java.io.File

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
            state.postValue(SeriesDetailState.DOWNLOADING(0.toPercentage()))
            useCase.invoke(
                    serie,
                    { onProgress(it) },
                    { onSuccess(it) },
                    { onPaused() },
                    { onResumed(it) },
                    { onDeleted() },
                    { onError(it) }
            )
        }
    }

    fun cancelDownloadChapter() {
        useCase.invokeCancel(serie)
    }

    private fun onProgress(progress: String) {
        state.postValue(SeriesDetailState.DOWNLOADING(progress))
    }

    private fun onSuccess(file: File) {
        serie.file = file.absolutePath
        state.postValue(SeriesDetailState.DOWNLOADED(null))
    }

    private fun onPaused() {
        state.postValue(SeriesDetailState.PAUSED)
    }

    private fun onResumed(progress: String) {
        state.postValue(SeriesDetailState.DOWNLOADING(progress))
    }

    private fun onDeleted() {
        state.postValue(SeriesDetailState.INITIAL)
    }

    private fun onError(message: String) {
        state.postValue(SeriesDetailState.ERROR(message))
    }

    fun getState(): LiveData<SeriesDetailState> = state

}
