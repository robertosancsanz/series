package com.android.es.roversanz.series.usecases.download

import android.arch.lifecycle.Observer
import android.util.Log
import com.android.es.roversanz.series.data.DownloadManager
import com.android.es.roversanz.series.data.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.DownloadManager.DownloadManagerState.PAUSED
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.series.SerieDownloaded

class PauseDownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    companion object {
        const val TAG = "Download"
    }

    private var observer: Observer<DownloadManagerState>? = null

    operator fun invoke(
            serie: Serie,
            callback: ((SerieDownloaded) -> Unit)? = null) {

        Log.d(TAG, "Pausing ${serie.title} on $this")

        callback?.let {
            if (observer == null) {
                observer = Observer<DownloadManagerState> { state ->
                    when (state) {
                        is PAUSED -> {
                            it.invoke(state.serieDownloaded)
                            removeObserver()
                        }
                    }
                }.apply { downloadManager.state.observeForever(this) }

            }
        }
        downloadManager.pause(serie.id)
    }

    private fun removeObserver() {
        observer?.let { downloadManager.state.removeObserver(it) }
        observer = null
    }
}
