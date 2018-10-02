package com.android.es.roversanz.series.presentation.ui.main

import android.arch.lifecycle.ViewModel
import com.android.es.roversanz.series.data.SerieRepository

class SeriesViewModel(private val seriesRepository: SerieRepository) : ViewModel() {

    init {
        seriesRepository.getSeries()
    }
}