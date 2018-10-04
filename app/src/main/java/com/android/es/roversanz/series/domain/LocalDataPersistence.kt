package com.android.es.roversanz.series.domain

import com.android.es.roversanz.series.data.DataPersistence

class LocalDataPersistence : DataPersistence {

    private val series: MutableList<Serie> by lazy {
        mutableListOf(
                Serie(1, "El Cuento de la Criada", "Muy heavy", "https://www.ecestaticos.com/imagestatic/clipping/03b/c8f/03bc8fb84150aa179e63bc8898802516/imagen-sin-titulo.jpg?mtime=1531473691"),
                Serie(2, "Picky Blinders ", "Son mu malos", "https://i.blogs.es/f8389e/espinof-critica-de-peaky-blinders-temporada-4/450_1000.jpg"))
    }

    override fun fetchSeries(): List<Serie> = series

    override fun fetchSeriesById(id: Long): Serie? = series.firstOrNull { it.id == id }

    override fun addSerie(serie: Serie) {
        series.add(serie)
    }
}
