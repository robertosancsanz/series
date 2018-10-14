package com.android.es.roversanz.series.usecases.series

import com.android.es.roversanz.series.domain.Serie

data class SerieDownloaded(val serie: Serie,
                           val state: String,
                           val error: String? = null,
                           val filePath: String? = null,
                           val progress: String? = null) {

    override fun equals(other: Any?): Boolean =
            this.serie == (other as? SerieDownloaded)?.serie ?: false

    @Suppress("MagicNumber")
    override fun hashCode(): Int {
        var result = serie.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (progress?.hashCode() ?: 0)
        return result
    }

}
