package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase

class SeriesDetailViewModelFactory(private val useCaseDownload: DownloadFileUseCase,
                                   private val useCasePauseDownload: PauseDownloadFileUseCase,
                                   private val useCaseResumeDownload: ResumeDownloadFileUseCase,
                                   private val useCaseCancelDownload: CancelDownloadFileUseCase,
                                   private val serie: Serie) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = SeriesDetailViewModel(useCaseDownload, useCasePauseDownload, useCaseResumeDownload, useCaseCancelDownload, serie) as? T
              ?: throw IllegalArgumentException("This factory can only create SeriesDetailViewModel instances")

}
