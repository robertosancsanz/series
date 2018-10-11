package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class SeriesListViewModelFactory(
        private val useCase: GetSeriesListUseCase,
        private val useCaseDownload: DownloadFileUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = SeriesListViewModel(useCase, useCaseDownload) as? T
              ?: throw IllegalArgumentException("This factory can only create SeriesListViewModel instances")

}
