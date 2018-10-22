package com.android.es.roversanz.series.usecases.download

import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.arch.lifecycle.LiveData
import android.content.ComponentName
import android.os.PersistableBundle
import com.android.es.roversanz.series.data.download.DownloadManager
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.download.DownloadService
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase

class DownloadFileUseCase(private val downloadManager: DownloadManager,
                          private val jobScheduler: JobScheduler?,
                          private val componentName: ComponentName) : UseCase {

    companion object {
        const val TAG = "Download"
    }

    val state: LiveData<DownloadManagerState>
        get() = downloadManager.state

    operator fun invoke(serie: Serie) {

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

}
