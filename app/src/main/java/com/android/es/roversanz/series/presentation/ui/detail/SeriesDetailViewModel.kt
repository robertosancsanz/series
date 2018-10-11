package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
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
        SeriesDetailState.PAUSED        -> useCase.invokeResume(serie)
        SeriesDetailState.DOWNLOADING   -> useCase.invokePaused(serie)
        is SeriesDetailState.DOWNLOADED -> state.postValue(DOWNLOADED(serie.file))
        else                            -> {
            state.postValue(SeriesDetailState.DOWNLOADING)
            useCase.invoke(serie, { onSuccess(it) }, { onPaused() }, { onResumed() }, { onError(it) })
        }
    }

    private fun onResumed() {
        state.postValue(SeriesDetailState.DOWNLOADING)
    }

    private fun onPaused() {
        state.postValue(SeriesDetailState.PAUSED)
    }

    private fun onSuccess(file: File) {
        serie.file = file.absolutePath
        state.postValue(SeriesDetailState.DOWNLOADED(null))
    }

    private fun onError(message: String) {
        state.postValue(SeriesDetailState.ERROR(message))
    }

    fun getState(): LiveData<SeriesDetailState> = state

}
