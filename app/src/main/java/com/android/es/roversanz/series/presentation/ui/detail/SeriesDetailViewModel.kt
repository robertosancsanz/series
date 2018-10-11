package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.CHECKPERMISSION
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase.DownloadFileUseCaseListener
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.toPercentage

class SeriesDetailViewModel(private val useCase: DownloadFileUseCase,
                            private val serie: Serie) : ViewModel(), DownloadFileUseCaseListener {

    private val state = MutableLiveData<SeriesDetailState>().apply {
        value = SeriesDetailState.INITIAL
    }

    init {
        state.postValue(SeriesDetailState.DONE(serie))
    }

    fun downloadChapter() = when (state.value) {
        SeriesDetailState.PAUSED             -> useCase.invokeResume(serie)
        is SeriesDetailState.DOWNLOADING     -> useCase.invokePaused(serie)
        is SeriesDetailState.DOWNLOADED      -> state.postValue(DOWNLOADED(serie.file))
        SeriesDetailState.CHECKPERMISSION -> useCase.invoke(serie, this)
        else                                 -> state.postValue(CHECKPERMISSION)
    }


    fun cancelDownloadChapter() {
        useCase.invokeCancel(serie)
    }

    //region DownloadFileUseCaseListener

    override fun onProgress(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    override fun onSuccess(serieDownloaded: SerieDownloaded) {
        this.serie.file = serieDownloaded.filePath
        state.postValue(SeriesDetailState.DOWNLOADED(null))
    }

    override fun onQueued(serieDownloaded: SerieDownloaded) {
        state.postValue(SeriesDetailState.DOWNLOADING(0.toPercentage()))
    }

    override fun onPaused(serieDownloaded: SerieDownloaded) {
        state.postValue(SeriesDetailState.PAUSED)
    }

    override fun onResumed(serieDownloaded: SerieDownloaded) {
        serieDownloaded.progress?.let { state.postValue(SeriesDetailState.DOWNLOADING(it)) }
    }

    override fun onDeleted(serieDownloaded: SerieDownloaded) {
        state.postValue(SeriesDetailState.INITIAL)
    }

    override fun onError(serieDownloaded: SerieDownloaded) {
        serieDownloaded.error?.let { state.postValue(SeriesDetailState.ERROR(it)) }
    }

    //endregion

    fun getState(): LiveData<SeriesDetailState> = state

}
