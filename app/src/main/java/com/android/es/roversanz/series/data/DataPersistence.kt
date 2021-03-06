package com.android.es.roversanz.series.data

import com.android.es.roversanz.series.domain.Serie

interface DataPersistence {

    fun fetchSeries(): List<Serie>

    fun fetchSeriesById(id: Long): Serie?

    fun addSerie(serie: Serie)

}
