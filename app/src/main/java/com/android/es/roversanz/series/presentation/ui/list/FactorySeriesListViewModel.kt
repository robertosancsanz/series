package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class FactorySeriesListViewModel(private val useCase: GetSeriesListUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = SeriesListViewModel(useCase) as? T
            ?: throw IllegalArgumentException("This factory can only create SeriesListViewModel instances")

}
