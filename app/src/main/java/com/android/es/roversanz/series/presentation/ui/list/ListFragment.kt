package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.FragmentScope
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.logger.Logger
import com.bumptech.glide.Glide
import dagger.Component
import kotlinx.android.synthetic.main.fragment_list_series.*
import kotlinx.android.synthetic.main.item_serie.view.*
import javax.inject.Inject

class ListFragment : Fragment() {

    companion object {

        private val TAG: String = ListFragment::class.java.simpleName
        private const val NUM_ITEMS: Int = 2

        fun getInstance() = ListFragment()
    }

    @Inject
    lateinit var factory: FactorySeriesListViewModel

    @Inject
    lateinit var logger: Logger

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

        viewModel.getState().observe(this, Observer { state -> state?.let { handleState(it) } })

        swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
    }

    private fun handleState(state: SeriesListState) {
        when (state) {
            SeriesListState.INITIAL -> {
                //Do nothing
            }
            SeriesListState.BUSY -> {
                swiperefresh.isRefreshing = true
            }
            SeriesListState.EMPTY -> {
                handleEmptyState()
            }
            is SeriesListState.DONE -> {
                handleSoneState(state.data)
            }
            is SeriesListState.ERROR -> {
                handleErrorState(state.message)
            }
        }
    }

    private fun handleEmptyState() {
        swiperefresh.isRefreshing = false
        series_empty_list.visibility = View.VISIBLE
        series_list.visibility = View.GONE
        series_error_list.visibility = View.GONE
    }

    private fun handleSoneState(list: List<Serie>) {
        swiperefresh.isRefreshing = false
        series_empty_list.visibility = View.GONE
        series_list.visibility = View.VISIBLE
        series_error_list.visibility = View.GONE
        updateList(list)
    }

    private fun handleErrorState(message: String) {
        logger.e(TAG, "Error: $message")
        swiperefresh.isRefreshing = false
        series_empty_list.visibility = View.GONE
        series_list.visibility = View.GONE
        series_error_list.visibility = View.VISIBLE
    }

    private fun updateList(list: List<Serie>) {
        logger.d("LIST", "Show the list: ${list.size}")
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

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SeriesViewHolder = SeriesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_serie, null))

    override fun getItemCount() = series.size

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.onBind(series[position])
    }

    fun updateSeries(list: List<Serie>) {
        series = list
        notifyDataSetChanged()
    }

    inner class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(serie: Serie) {
            itemView.serie_title.text = serie.title
            itemView.serie_description.text = serie.description
            Glide.with(itemView.context).load(serie.url).into(itemView.serie_image)

        }

    }

}
