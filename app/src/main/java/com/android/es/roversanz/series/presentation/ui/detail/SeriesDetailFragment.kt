package com.android.es.roversanz.series.presentation.ui.detail

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.FragmentScope
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.BUSY
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DONE
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADED
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.DOWNLOADING
import com.android.es.roversanz.series.presentation.ui.detail.SeriesDetailState.PAUSED
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.setVisibility
import com.bumptech.glide.Glide
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_description
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_download_button
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_image
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_loading
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_subtitle
import kotlinx.android.synthetic.main.fragment_detail_serie.serie_title
import javax.inject.Inject

class SeriesDetailFragment : Fragment() {

    companion object {

        private val TAG: String = SeriesDetailFragment::class.java.simpleName
        private const val SERIE_TAG: String = "serie_tag"

        fun getInstance(serie: Serie): SeriesDetailFragment = SeriesDetailFragment().apply {
            arguments = Bundle().apply { putParcelable(SERIE_TAG, serie) }
        }
    }

    @Inject
    lateinit var factory: SeriesDetailViewModelFactory

    private val viewModel: SeriesDetailViewModel by lazy {
        ViewModelProviders.of(this, factory)[SeriesDetailViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_detail_serie, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            inject(it.app())
            ActivityCompat.requestPermissions(it, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 123)
        }

        viewModel.getState().observe(this, Observer { state -> state?.let { handleState(it) } })

        serie_download_button.setOnClickListener { viewModel.downloadChapter() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.getState().removeObservers(this)
    }

    //region handle state

    private fun handleState(state: SeriesDetailState) {
        serie_loading.setVisibility(state == BUSY || state == DOWNLOADING)

        when (state) {
            is DONE        -> bindItem(state.serie)
            is PAUSED      -> serie_download_button.text = getString(R.string.button_resume)
            is DOWNLOADING -> serie_download_button.text = getString(R.string.button_pause)
            is DOWNLOADED  -> {
                serie_download_button.isEnabled = false
                serie_download_button.text = getString(R.string.button_already_download)
            }
            else           -> { //Do nothing
            }
        }
    }

    //endregion

    private fun bindItem(serie: Serie) {
        serie_title.text = serie.title
        serie_subtitle.text = serie.subtitle
        serie_description.text = serie.description
        context?.let { ctx -> Glide.with(ctx).load(serie.picture).into(serie_image) }
    }

    private fun inject(app: MyApplication) {
        DaggerSeriesDetailFragment_SeriesDetailFragmentComponent.builder()
                .mainComponent(app.component)
                .seriesDetailFragmentModule(SeriesDetailFragmentModule())
                .build()
                .inject(this)
    }

    //region di

    @FragmentScope
    @Component(dependencies = [(MainComponent::class)], modules = [SeriesDetailFragmentModule::class])
    internal interface SeriesDetailFragmentComponent {

        fun inject(fragment: SeriesDetailFragment)
    }

    @Module
    inner class SeriesDetailFragmentModule {

        @Provides
        @FragmentScope
        internal fun provideSeriesDetailViewModelFactory(useCase: DownloadFileUseCase):
                SeriesDetailViewModelFactory {
            val serie: Serie = arguments?.get(SERIE_TAG) as? Serie
                               ?: throw ClassCastException("SeriesDetailFragment must initialize  with an item")
            return SeriesDetailViewModelFactory(useCase, serie)
        }
    }

    //endregion
}
