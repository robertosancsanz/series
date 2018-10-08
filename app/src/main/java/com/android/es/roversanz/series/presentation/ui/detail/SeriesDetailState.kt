package com.android.es.roversanz.series.presentation.ui.detail

import com.android.es.roversanz.series.domain.Serie

sealed class SeriesDetailState {

    object INITIAL : SeriesDetailState()

    object BUSY : SeriesDetailState()

    class DONE(val data: Serie) : SeriesDetailState()

    class ERROR(val message: String = "") : SeriesDetailState()

}
