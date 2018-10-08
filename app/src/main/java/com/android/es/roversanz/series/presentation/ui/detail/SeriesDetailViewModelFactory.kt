package com.android.es.roversanz.series.presentation.ui.detail

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.android.es.roversanz.series.domain.Serie

class SeriesDetailViewModelFactory(private val serie: Serie) : ViewModelProvider
                                                               .Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = SeriesDetailViewModel(serie)as? T
              ?: throw IllegalArgumentException("This factory can only create SeriesDetailViewModel instances")

}
