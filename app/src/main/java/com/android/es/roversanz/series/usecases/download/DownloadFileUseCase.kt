package com.android.es.roversanz.series.usecases.download

import com.android.es.roversanz.series.data.DownloadManager
import com.android.es.roversanz.series.data.DownloadManager.DownloadFileUseCaseListener
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class DownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    operator fun invoke(
            serieToDownload: Serie,
            onSuccess: (SerieDownloaded) -> Unit,
            onError: (SerieDownloaded) -> Unit,
            onQueued: ((SerieDownloaded) -> Unit)? = null,
            onProgress: ((SerieDownloaded) -> Unit)? = null) {

        val listener = object : DownloadFileUseCaseListener {
            override fun onQueued(serieDownloaded: SerieDownloaded) {
                onQueued?.invoke(serieDownloaded)
            }

            override fun onProgress(serieDownloaded: SerieDownloaded) {
                onProgress?.invoke(serieDownloaded)
            }

            override fun onSuccess(serieDownloaded: SerieDownloaded) {
                onSuccess.invoke(serieDownloaded)
                downloadManager.removeCallbacks(this)
            }

            override fun onError(serieDownloaded: SerieDownloaded) {
                onError.invoke(serieDownloaded)
                downloadManager.removeCallbacks(this)
            }
        }

        downloadManager.addToCallbacks(listener)
        downloadManager.download(serieToDownload)
    }

}
