package com.android.es.roversanz.series.usecases.download

import com.android.es.roversanz.series.data.DownloadManager
import com.android.es.roversanz.series.data.DownloadManager.DownloadFileUseCaseListener
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class ResumeDownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    operator fun invoke(
            serie: Serie,
            callback: ((SerieDownloaded) -> Unit)? = null) {

        callback?.let { cb ->
            val listener = object : DownloadFileUseCaseListener {
                override fun onResumed(serieDownloaded: SerieDownloaded) {
                    cb.invoke(serieDownloaded)
                    downloadManager.removeCallbacks(this)
                }
            }.apply { downloadManager.addToCallbacks(this) }
        }
        downloadManager.resume(serie.id)
    }


}
