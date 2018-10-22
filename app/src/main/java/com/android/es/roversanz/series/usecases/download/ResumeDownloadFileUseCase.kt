package com.android.es.roversanz.series.usecases.download

import android.arch.lifecycle.Observer
import android.util.Log
import com.android.es.roversanz.series.data.download.DownloadManager
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.RESUMED
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class ResumeDownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    companion object {
        const val TAG = "Download"
    }

    operator fun invoke(serie: Serie) {


        Log.d(TAG, "Resuming ${serie.title}")
        downloadManager.resume(serie.id)
    }

}
