package com.android.es.roversanz.series.usecases.download

import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.os.PersistableBundle
import com.android.es.roversanz.series.data.download.DownloadManager
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.COMPLETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.ERROR
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PROGRESS
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.QUEUED
import com.android.es.roversanz.series.data.download.DownloadService
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded


class DownloadFileUseCase(private val downloadManager: DownloadManager,
                          private val jobScheduler: JobScheduler?,
                          private val componentName: ComponentName) : UseCase {

    companion object {
        const val TAG = "Download"
    }

    private var observer: Observer<DownloadManagerState>? = null

    operator fun invoke(
            serie: Serie,
            onSuccess: (SerieDownloaded) -> Unit,
            onError: (SerieDownloaded) -> Unit,
            onQueued: ((SerieDownloaded) -> Unit)? = null,
            onProgress: ((SerieDownloaded) -> Unit)? = null) {

        //Log.d(TAG, "Downloading ${serie.title} on $this")
//        //FIXME: The observable is the same, but in this way, it´s overrinding the callbacks, so losing the first one
////        if (observer == null) {
        observer = Observer<DownloadManagerState> { state ->
            when (state) {
                is QUEUED    -> {
//                    Log.d(TAG, "Queued ${serie.title}")
                    onQueued?.invoke(state.serieDownloaded)
                }
                is PROGRESS  -> {
//                    Log.d(TAG, "PROGRESS ${serie.title}")
                    onProgress?.invoke(state.serieDownloaded)
                }
                is COMPLETED -> {
//                    Log.d(TAG, "COMPLETED ${serie.title}")
                    onSuccess.invoke(state.serieDownloaded)
//                        removeObserver()
                }
                is ERROR     -> {
//                    Log.d(TAG, "ERROR ${serie.title}")
                    onError.invoke(state.serieDownloaded)
//                        removeObserver()
                }
            }
        }.apply { downloadManager.state.observeForever(this) }
//
////        }

        //Start download
        jobScheduler?.schedule(JobInfo.Builder(jobScheduler.allPendingJobs.size, componentName)
                                       .setRequiredNetworkType(NETWORK_TYPE_ANY)
                                       .setExtras(PersistableBundle().apply {
                                           putInt(DownloadService.FIELD_ID, serie.id)
                                           putString(DownloadService.FIELD_TITLE, serie.title)
                                           putString(DownloadService.FIELD_SUBTITLE, serie.subtitle)
                                           putString(DownloadService.FIELD_DESCRIPTION, serie.description)
                                           putString(DownloadService.FIELD_PICTURE, serie.picture)
                                           putString(DownloadService.FIELD_URL, serie.downloadUrl)
                                       }).build())
    }


    private fun removeObserver() {
        observer?.let { downloadManager.state.removeObserver(it) }
        observer = null
    }

}
