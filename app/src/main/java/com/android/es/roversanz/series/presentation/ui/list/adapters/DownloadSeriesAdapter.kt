package com.android.es.roversanz.series.presentation.ui.list.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.utils.inflate
import kotlinx.android.synthetic.main.item_serie.view.serie_description
import kotlinx.android.synthetic.main.item_serie.view.serie_title

class DownloadSeriesAdapter(
        private val listener: ((Serie) -> Unit)?,
        private val downloadListener: ((Serie) -> Unit)?) : RecyclerView.Adapter<DownloadSeriesAdapter.SeriesViewHolder>() {

    private var series: MutableList<Serie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SeriesViewHolder = SeriesViewHolder(parent.inflate(R.layout.item_serie_downloading))

    override fun getItemCount() = series.size

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        series[position].let { serie ->
            holder.onBind(serie)
        }
    }

    fun addSerie(serie: Serie) {
        if (!series.contains(serie)) {
            series.add(serie)
            notifyItemChanged(series.size - 1)
        }
    }

    class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(serie: Serie) {
            itemView.serie_title.text = serie.title
            itemView.serie_description.text = serie.subtitle
        }

    }

}