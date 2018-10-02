package com.android.es.roversanz.series.domain

import com.android.es.roversanz.series.data.DataPersistence

class LocalDataPersistence : DataPersistence {

    private val series: MutableList<Serie> by lazy {
        mutableListOf(
                Serie(1, "El Cuento de la Criada", "Muy heavy"),
                Serie(2, "Picky Blinders ", "Son mu malos"))
    }

    override fun fetchSeries(): List<Serie> = series

    override fun fetchSeriesById(id: Long): Serie? = series.firstOrNull { it.id == id }

    override fun addSerie(serie: Serie) {
        series.add(serie)
    }
}
