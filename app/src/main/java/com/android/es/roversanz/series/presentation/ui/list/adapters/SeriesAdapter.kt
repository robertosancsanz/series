package com.android.es.roversanz.series.presentation.ui.list.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.utils.inflate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_serie.view.serie_description
import kotlinx.android.synthetic.main.item_serie.view.serie_download_button
import kotlinx.android.synthetic.main.item_serie.view.serie_image
import kotlinx.android.synthetic.main.item_serie.view.serie_title

class SeriesAdapter(
        private val listener: ((Serie) -> Unit)?,
        private val downloadListener: ((Serie) -> Unit)?) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    private var series: List<Serie> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SeriesViewHolder = SeriesViewHolder(parent.inflate(R.layout.item_serie))

    override fun getItemCount() = series.size

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        series[position].let { serie ->
            holder.apply {
                onBind(serie)
                itemView.apply {
                    setOnClickListener {
                        listener?.invoke(serie)
                    }
                    serie_download_button.setOnClickListener {
                        downloadListener?.invoke(serie)
                    }
                }
            }
        }
    }

    fun updateSeries(list: List<Serie>) {
        series = list
        notifyDataSetChanged()
    }

    class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(serie: Serie) {
            itemView.serie_title.text = serie.title
            itemView.serie_description.text = serie.subtitle
            Glide.with(itemView.context).load(serie.picture).into(itemView.serie_image)
        }

    }

}