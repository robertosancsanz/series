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
import com.android.es.roversanz.series.utils.inflate
import com.android.es.roversanz.series.utils.logger.Logger
import com.bumptech.glide.Glide
import dagger.Component
import kotlinx.android.synthetic.main.fragment_list_series.series_empty_list
import kotlinx.android.synthetic.main.fragment_list_series.series_error_list
import kotlinx.android.synthetic.main.fragment_list_series.series_list
import kotlinx.android.synthetic.main.fragment_list_series.swiperefresh
import kotlinx.android.synthetic.main.item_serie.view.serie_description
import kotlinx.android.synthetic.main.item_serie.view.serie_image
import kotlinx.android.synthetic.main.item_serie.view.serie_title
import javax.inject.Inject

class SeriesListFragment : Fragment() {

    companion object {

        private val TAG: String = SeriesListFragment::class.java.simpleName
        private const val NUM_ITEMS: Int = 2

        fun getInstance() = SeriesListFragment()
    }

    @Inject
    lateinit var factory: SeriesListViewModelFactory

    @Inject
    lateinit var logger: Logger

    private lateinit var seriesAdapter: SeriesAdapter
    private lateinit var callback: SeriesListFragmentListener
    private val viewModel: SeriesListViewModel by lazy {
        ViewModelProviders.of(this, factory)[SeriesListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_list_series, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            inject(it.app())
            callback = it as? SeriesListFragmentListener
                    ?: throw ClassCastException("${it::class.java.simpleName} must implements ListFragmentListener")
        }

        seriesAdapter = SeriesAdapter {
            logger.d(TAG, "Click on item: ${it.title}")
            callback.onSerieSelected(it)
        }

        series_list.apply {
            adapter = seriesAdapter
            layoutManager = GridLayoutManager(context, NUM_ITEMS)
        }

        viewModel.getState().observe(this, Observer { state ->
            state?.let { handleState(it) }
        })

        swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
    }

    //region handle state

    private fun handleState(state: SeriesListState) {
        swiperefresh.isRefreshing = state == SeriesListState.BUSY

        series_empty_list.setVisibility(state == SeriesListState.EMPTY)
        series_list.setVisibility(state is SeriesListState.DONE)
        series_error_list.setVisibility(state is SeriesListState.ERROR)

        if (state is SeriesListState.DONE) {
            updateList(state.data)
        }
    }

    private fun View.setVisibility(visibility: Boolean) {
        if (visibility) {
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }

    //endregion

    private fun updateList(list: List<Serie>) {
        logger.d("LIST", "Show the list: ${list.size}")
        seriesAdapter.updateSeries(list)
    }

    private fun inject(app: MyApplication) {
        DaggerSeriesListFragment_SeriesListFragmentComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @FragmentScope
    @Component(dependencies = [(MainComponent::class)])
    internal interface SeriesListFragmentComponent {

        fun inject(fragment: SeriesListFragment)
    }


    private inner class SeriesAdapter(private val listener: ((Serie) -> Unit)?) :
            RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

        private var series: List<Serie> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, type: Int): SeriesViewHolder = SeriesViewHolder(parent.inflate(R.layout.item_serie))

        override fun getItemCount() = series.size

        override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
            series[position].let { serie ->
                holder.onBind(serie)
                holder.itemView.setOnClickListener {
                    listener?.invoke(serie)
                }
            }
        }

        fun updateSeries(list: List<Serie>) {
            series = list
            notifyDataSetChanged()
        }

        private inner class SeriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun onBind(serie: Serie) {
                itemView.serie_title.text = serie.title
                itemView.serie_description.text = serie.subtitle
                Glide.with(itemView.context).load(serie.picture).into(itemView.serie_image)
            }

        }

    }

    interface SeriesListFragmentListener {
        fun onSerieSelected(serie: Serie)
    }

}
