package com.android.es.roversanz.series.presentation.ui.detail

import com.android.es.roversanz.series.domain.Serie

sealed class SeriesDetailState {

    object INITIAL : SeriesDetailState()

    object BUSY : SeriesDetailState()

    object DOWNLOADING: SeriesDetailState()

    object PAUSED: SeriesDetailState()

    object DOWNLOADED: SeriesDetailState()

    class DONE(val serie: Serie) : SeriesDetailState()

    class ERROR(val message: String = "") : SeriesDetailState()

}
