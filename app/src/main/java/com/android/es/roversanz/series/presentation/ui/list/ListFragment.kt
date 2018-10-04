package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.FragmentScope
import com.android.es.roversanz.series.utils.app
import dagger.Component
import kotlinx.android.synthetic.main.fragment_list_series.*
import kotlinx.android.synthetic.main.item_serie.view.*
import javax.inject.Inject

class ListFragment : Fragment() {

    companion object {
        fun getInstance() = ListFragment()
    }

    @Inject
    lateinit var factory: FactorySeriesListViewModel

    private val viewModel: SeriesListViewModel by lazy {
        ViewModelProviders.of(this, factory)[SeriesListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.fragment_list_series, null)

    private lateinit var seriesAdapter: SeriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { inject(it.app()) }

        seriesAdapter = SeriesAdapter()
        series_list.apply {
            adapter = seriesAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        viewModel.getState().observe(this, Observer { state ->
            when (state) {
                SeriesListState.INITIAL -> Log.d("LIST", "Initial State")
                SeriesListState.BUSY -> Log.d("LIST", "Show Loading")
                SeriesListState.EMPTY -> Log.d("LIST", "The list is empty")
                is SeriesListState.DONE -> updateList(state.data)
                is SeriesListState.ERROR -> Log.d("LIST", "Show the error")
                else -> Log.d("LIST", "Something is wrong")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
    }

    private fun updateList(list: List<Serie>) {
        Log.d("LIST", "Show the list: ${list.size}")
        seriesAdapter.updateSeries(list)
    }

    private fun inject(app: MyApplication) {
        DaggerListFragment_ListFragmentComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @FragmentScope
    @Component(dependencies = [(MainComponent::class)])
    internal interface ListFragmentComponent {

        fun inject(fragment: ListFragment)
    }

}

class SeriesAdapter : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    private var series: List<Serie> = listOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SeriesViewHolder {
        return SeriesViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_serie, null))
    }

    override fun getItemCount() = series.size

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.onBind(series[position])
    }

    fun updateSeries(list: List<Serie>) {
        series = list
        notifyDataSetChanged()
    }

    class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(serie: Serie) {
            itemView.serie_title.text = serie.title
            itemView.serie_description.text = serie.description
        }

    }

}
