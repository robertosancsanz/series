package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase

class SeriesViewModel(private val useCase: GetSeriesListUseCase) : ViewModel() {

    init {
        useCase.invoke({}, {})
    }
}
