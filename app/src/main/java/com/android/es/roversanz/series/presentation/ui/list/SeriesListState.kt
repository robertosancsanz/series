package com.android.es.roversanz.series.presentation.ui.list

import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

sealed class SeriesListState {

    object INITIAL : SeriesListState()

    object BUSY : SeriesListState()

    object EMPTY : SeriesListState()

    class DONE(val data: List<Serie>) : SeriesListState()

    class ERROR(val message: String = "") : SeriesListState()

}

sealed class DownloadSerieState {

    object INITIAL : DownloadSerieState()

    object CHECKPERMISSION : DownloadSerieState()

    class DOWNLOAD(val serieDownloaded: SerieDownloaded) : DownloadSerieState()

    class DOWNLOADED(val serieDownloaded: SerieDownloaded) : DownloadSerieState()

    class ERROR(val serieDownloaded: SerieDownloaded) : DownloadSerieState()

    class REMOVE(val serieDownloaded: SerieDownloaded) : DownloadSerieState()

}
