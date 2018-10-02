package com.android.es.roversanz.series.presentation.ui.list

import com.android.es.roversanz.series.domain.Serie

sealed class SeriesListState {

    object INITIAL : SeriesListState()

    object BUSY : SeriesListState()

    object EMPTY : SeriesListState()

    class DONE(val data: List<Serie>) : SeriesListState()

    class ERROR(val message: String = "") : SeriesListState()

}
