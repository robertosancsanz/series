package com.android.es.roversanz.series.usecases.series

import com.android.es.roversanz.series.domain.Serie

data class SerieDownloaded(val serie: Serie,
                           val state: String,
                           val error: String? = null,
                           val filePath: String? = null,
                           val progress: String? = null) {

    fun customError(): String = this.serie.title + ": " + error

    override fun equals(other: Any?): Boolean {
        return this.serie == (other as SerieDownloaded).serie
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}