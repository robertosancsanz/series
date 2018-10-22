package com.android.es.roversanz.series.usecases.download

import android.util.Log
import com.android.es.roversanz.series.data.download.DownloadManager
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase

class PauseDownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    companion object {
        const val TAG = "Download"
    }

    operator fun invoke(serie: Serie) {

        Log.d(TAG, "Pausing ${serie.title}")
        downloadManager.pause(serie.id)
    }

}
