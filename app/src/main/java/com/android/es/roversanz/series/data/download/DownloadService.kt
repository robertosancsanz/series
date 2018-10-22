package com.android.es.roversanz.series.data.download

import android.app.job.JobParameters
import android.app.job.JobService
import android.arch.lifecycle.Observer
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.ServiceScope
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Component
import java.security.InvalidParameterException
import javax.inject.Inject

class DownloadService : JobService() {

    companion object {
        val TAG: String = DownloadService::class.java.simpleName
        const val FIELD_ID = "id"
        const val FIELD_TITLE = "title"
        const val FIELD_SUBTITLE = "subtitle"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PICTURE = "picture"
        const val FIELD_URL = "url"
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        inject(application as MyApplication)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        doSampleJob(params)
        return true
    }

    private val observer = Observer<DownloadManagerState> {
        it?.let { status ->
            //            logger.d(TAG, "Status: $status")
            //TODO: show notification
//            when (status) {
//            }
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        logger.d(TAG, "onStopJob")
        val id = params?.extras?.getInt("id")
//        downloadManager.state.removeObserver { observer }
        id?.let { downloadManager.cancel(it) }
        return false
    }

    private fun doSampleJob(params: JobParameters?) {
        params?.extras?.let { bundle ->
            val serie = Serie(
                    id = bundle.getInt(FIELD_ID),
                    title = bundle.getString(FIELD_TITLE).orEmpty(),
                    subtitle = bundle.getString(FIELD_SUBTITLE).orEmpty(),
                    picture = bundle.getString(FIELD_PICTURE).orEmpty(),
                    description = bundle.getString(FIELD_DESCRIPTION).orEmpty(),
                    downloadUrl = bundle.getString(FIELD_URL).orEmpty()
            )
            logger.d(TAG, "Serie ${serie.id}: ${serie.title}")
            downloadManager.download(serie)
            //        downloadManager.state.observeForever(observer)
        } ?: throw InvalidParameterException("It´s needed send all the parameters")

        jobFinished(params, false)
    }

    //region di

    private fun inject(app: MyApplication) {
        DaggerDownloadService_DownloadServiceComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @Component(dependencies = [(MainComponent::class)])
    @ServiceScope
    internal interface DownloadServiceComponent {

        fun inject(service: DownloadService)
    }

    //endregion

}

//fun String?.orEmpty(): String = this ?: String.toString().orEmpty()""