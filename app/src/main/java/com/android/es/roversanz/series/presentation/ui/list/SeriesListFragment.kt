package com.android.es.roversanz.series.presentation.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.FragmentScope
import com.android.es.roversanz.series.presentation.ui.list.adapters.DownloadSeriesAdapter
import com.android.es.roversanz.series.presentation.ui.list.adapters.SeriesAdapter
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.permission.Permission
import com.android.es.roversanz.series.utils.permission.PermissionHandler
import com.android.es.roversanz.series.utils.permission.hasPermission
import com.android.es.roversanz.series.utils.permission.onRequestPermission
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.setVisibility
import com.android.es.roversanz.series.utils.snack
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.fragment_list_series.download_list
import kotlinx.android.synthetic.main.fragment_list_series.series_empty_list
import kotlinx.android.synthetic.main.fragment_list_series.series_error_list
import kotlinx.android.synthetic.main.fragment_list_series.series_list
import kotlinx.android.synthetic.main.fragment_list_series.swiperefresh
import javax.inject.Inject

class SeriesListFragment : Fragment() {

    companion object {
        private const val NUM_ITEMS: Int = 2
        private const val REQUEST_CODE: Int = 123

        fun getInstance() = SeriesListFragment()
    }

    @Inject
    lateinit var factory: SeriesListViewModelFactory

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private lateinit var seriesAdapter: SeriesAdapter
    private lateinit var downloadAdapter: DownloadSeriesAdapter

    private lateinit var callback: SeriesListFragmentListener
    private val viewModel: SeriesListViewModel by lazy {
        ViewModelProviders.of(this, factory)[SeriesListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_list_series, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            inject(it.app())
            callback = it as? SeriesListFragmentListener
                    ?: throw ClassCastException("${it::class.java.simpleName} must implements ListFragmentListener")
        }

        seriesAdapter = SeriesAdapter({ callback.onSerieSelected(it) }, { viewModel.onSerieDownload(it) })
        downloadAdapter = DownloadSeriesAdapter(
                resourceProvider,
                onPause = { serie -> viewModel.onPause(serie) },
                onResume = { serie -> viewModel.onResume(serie) },
                onRemove = { serie -> viewModel.onRemove(serie) },
                onPlay = { path -> playSerie(path) }
        )

        series_list.apply {
            adapter = seriesAdapter
            layoutManager = GridLayoutManager(context, NUM_ITEMS)
        }

        download_list.apply {
            adapter = downloadAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.apply {
            getState().observe(this@SeriesListFragment, Observer { state ->
                state?.let { handleState(it) }
            })
            getDownloadState().observe(this@SeriesListFragment, Observer { state ->
                state?.let { handleDownloadState(it) }
            })
        }

        swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
    }

    //region Permission

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val perm = PermissionHandler.getPermissionFromRequestCode(requestCode)
        perm?.let {
            when {
                PermissionHandler.isPermissionGranted(it, requestCode, grantResults) -> viewModel.onSerieDownload()
                PermissionHandler.isPermissionDenied(it, requestCode, grantResults)  -> onRequestPermission(it)
                else                                                                 -> onRequestPermission(it)
            }
        }
    }

    private fun checkPermission() {
        if (context.hasPermission(Permission.STORAGE)) {
            viewModel.onSerieDownload()
        } else {
            onRequestPermission(Permission.STORAGE)
        }
    }

    //endregion

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

    private fun handleDownloadState(state: DownloadSerieState) {
        when (state) {
            DownloadSerieState.CHECKPERMISSION -> checkPermission()
            is DownloadSerieState.DOWNLOAD     -> downloadAdapter.addSerie(state.serieDownloaded)
            is DownloadSerieState.DOWNLOADED   -> downloadAdapter.addSerie(state.serieDownloaded)
            is DownloadSerieState.REMOVE       -> downloadAdapter.removeSerie(state.serieDownloaded)
            is DownloadSerieState.ERROR        -> {
                downloadAdapter.addSerie(state.serieDownloaded)
                view?.snack(state.serieDownloaded.customError(), Snackbar.LENGTH_SHORT)
            }
        }

        download_list.setVisibility(downloadAdapter.itemCount > 0)
    }

    //endregion

    private fun updateList(list: List<Serie>) {
        logger.d("LIST", "Show the list: ${list.size}")
        seriesAdapter.updateSeries(list)
    }

    private fun playSerie(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path)).apply {
            setDataAndType(Uri.parse(path), "video/mp4")
        }
        activity?.startActivity(intent)
    }

    private fun inject(app: MyApplication) {
        DaggerSeriesListFragment_SeriesListFragmentComponent.builder()
                .mainComponent(app.component)
                .seriesDetailFragmentModule(SeriesDetailFragmentModule())
                .build()
                .inject(this)
    }

    //region di

    @FragmentScope
    @Component(dependencies = [(MainComponent::class)], modules = [SeriesDetailFragmentModule::class])
    internal interface SeriesListFragmentComponent {

        fun inject(fragment: SeriesListFragment)
    }

    @Module
    inner class SeriesDetailFragmentModule {

        @Provides
        @FragmentScope
        internal fun provideSeriesListViewModelFactory(useCase: GetSeriesListUseCase, usecaseDownload: DownloadFileUseCase) = SeriesListViewModelFactory(useCase, usecaseDownload)
    }

    //endregion

    interface SeriesListFragmentListener {
        fun onSerieSelected(serie: Serie)
    }

}
