package com.android.es.roversanz.series.presentation.ui.list.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.inflate
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.tonyodev.fetch2.Status
import kotlinx.android.synthetic.main.item_serie_downloading.view.serie_percentage
import kotlinx.android.synthetic.main.item_serie_downloading.view.serie_status
import kotlinx.android.synthetic.main.item_serie_downloading.view.serie_subtitle
import kotlinx.android.synthetic.main.item_serie_downloading.view.serie_title

class DownloadSeriesAdapter(
        private val resourceProvider: ResourceProvider,
        private val onPause: (Serie) -> Unit,
        private val onResume: (Serie) -> Unit,
        private val onRemove: (Serie) -> Unit,
        private val onPlay: (String) -> Unit) : RecyclerView.Adapter<DownloadSeriesAdapter
.SeriesViewHolder>() {

    companion object {
        private val TAG: String = "DOWNLOADED"
    }

    private var series: MutableList<SerieDownloaded> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SeriesViewHolder
            = SeriesViewHolder(parent.inflate(R.layout.item_serie_downloading))

    override fun getItemCount() = series.size

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        series[position].let { serieToDownload ->
            holder.apply {
                onBind(serieToDownload)
                itemView.setOnClickListener {
                    when (serieToDownload.state) {
                        Status.FAILED.name, Status.CANCELLED.name   -> onRemove.invoke(serieToDownload.serie)
                        Status.QUEUED.name, Status.DOWNLOADING.name -> onPause.invoke(serieToDownload.serie)
                        Status.PAUSED.name                          -> onResume.invoke(serieToDownload.serie)
                        Status.COMPLETED.name                       -> serieToDownload.filePath?.let { path ->
                            onPlay.invoke(path)
                        }
                        else                                        -> {
                        }
                    }
                }
            }
        }
    }

    fun addSerie(serie: SerieDownloaded) {
        if (series.contains(serie)) {
            series.replace(serie, serie)
        } else {
            Log.d(TAG, "Adding: ${serie.serie.title} on state ${serie.state}")
            series.add(serie)
        }
        notifyItemChanged(series.indexOf(serie))
    }

    fun removeSerie(serie: SerieDownloaded) {
        if (series.contains(serie)) {
            series.remove(serie)
        }
    }

    inner class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(serie: SerieDownloaded) {
            itemView.setBackgroundColor(resourceProvider.getColor(getBackgroundColor(serie.state)))

            itemView.serie_title.text = serie.serie.title
            itemView.serie_subtitle.text = serie.serie.subtitle
            itemView.serie_status.text = serie.state
            itemView.serie_percentage.text = serie.progress
        }

        //TODO: Replace Ugly strings
        private fun getBackgroundColor(state: String): Int = when (state) {
            Status.FAILED.name, Status.CANCELLED.name   -> R.color.color_error
            Status.QUEUED.name, Status.DOWNLOADING.name -> R.color.color_progress
            Status.COMPLETED.name                       -> R.color.color_complete
            else                                        -> R.color.colorPrimaryLight
        }

    }

}

fun <E> MutableList<E>.replace(old: E, new: E): List<E> {
    val ind = indexOf(old)
    this[ind] = new
    return this
}
