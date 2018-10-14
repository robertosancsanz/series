package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class SeriesListViewModelFactory(
        private val useCase: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase,
        private val useCasePauseDownload: PauseDownloadFileUseCase,
        private val useCaseResumeDownload: ResumeDownloadFileUseCase,
        private val useCaseCancelDownload: CancelDownloadFileUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = SeriesListViewModel(
            useCase,
            useCaseDownload,
            useCasePauseDownload,
            useCaseResumeDownload,
            useCaseCancelDownload) as? T
              ?: throw IllegalArgumentException("This factory can only create SeriesListViewModel instances")

}
