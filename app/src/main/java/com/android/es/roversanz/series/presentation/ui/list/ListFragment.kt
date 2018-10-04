package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.FragmentScope
import com.android.es.roversanz.series.utils.app
import dagger.Component
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.fragment_list_series, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { inject(it.app()) }

        viewModel.getState().observe(this, Observer { state ->
            when (state) {
                SeriesListState.INITIAL -> Log.d("LIST", "Initial State")
                SeriesListState.BUSY -> Log.d("LIST", "Show Loading")
                SeriesListState.EMPTY -> Log.d("LIST", "The list is empty")
                is SeriesListState.DONE -> Log.d("LIST", "Show the list: ${state.data.size}")
                is SeriesListState.ERROR -> Log.d("LIST", "Show the error")
                else -> Log.d("LIST", "Something is wrong")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
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
