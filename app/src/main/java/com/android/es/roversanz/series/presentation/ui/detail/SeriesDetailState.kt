package com.android.es.roversanz.series.presentation.ui.detail

import com.android.es.roversanz.series.domain.Serie

sealed class SeriesDetailState {

    object INITIAL : SeriesDetailState()

    object BUSY : SeriesDetailState()

    object CHECKPERMISSION : SeriesDetailState()

    class DOWNLOADING(val progress: String) : SeriesDetailState()

    object PAUSED : SeriesDetailState()

    class DOWNLOADED(val filePath: String?) : SeriesDetailState()

    class DONE(val serie: Serie) : SeriesDetailState()

    class ERROR(val message: String = "") : SeriesDetailState()

}
