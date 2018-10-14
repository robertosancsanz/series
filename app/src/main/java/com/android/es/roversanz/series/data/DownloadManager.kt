package com.android.es.roversanz.series.data

import android.os.Environment
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.toPercentage
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.NetworkType.WIFI_ONLY
import com.tonyodev.fetch2.Priority.NORMAL
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import java.io.File

class DownloadManager(
        private val logger: Logger,
        private val fetch: Fetch,
        private val resourceProvider: ResourceProvider) : FetchListener {

    companion object {
        private const val TAG: String = "DOWNLOADED"
        private val PATH: String = Environment.DIRECTORY_DOWNLOADS
        private const val DIRECTORY = "Series/"
    }

    private val seriesMap = mutableMapOf<Long, Serie>()
    private var callbacks = mutableListOf<DownloadFileUseCaseListener>()

    init {
        fetch.addListener(this)
    }

    //region listener

    override fun onAdded(download: Download) {
        updateStatus(download)
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.forEach { callback ->
                callback.onQueued(SerieDownloaded(serie = serie, state = download.status.name,
                                                  progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
        updateStatus(download)
    }

    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.forEach { callback ->
                callback.onProgress(SerieDownloaded(serie = serie, state = download.status
                        .name, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

    }

    override fun onPaused(download: Download) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.iterator().forEach { callback ->
                callback.onPaused(SerieDownloaded(serie = serie, state = download.status
                        .name, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onResumed(download: Download) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.iterator().forEach { callback ->
                callback.onResumed(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onCompleted(download: Download) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            val file = getFile(serie)
            logger.d(TAG, "onCompleted: ${download.id} -- ${serie.title}")

            callbacks.iterator().forEach { callback ->
                callback.onSuccess(SerieDownloaded(serie = serie, state = download.status.name,
                                                   progress = download.progress.toPercentage(), filePath = file.absolutePath))
            }
        }
    }

    override fun onCancelled(download: Download) {
        updateStatus(download)
        fetch.remove(download.id)
    }

    override fun onRemoved(download: Download) {
        updateStatus(download)
    }

    override fun onDeleted(download: Download) {
        updateStatus(download)
        fetch.remove(download.id)
        seriesMap[download.request.identifier]?.let { serie ->
            removeFile(serie)
            callbacks.iterator().forEach { callback ->
                callback.onDeleted(SerieDownloaded(serie = serie, state = download.status
                        .name))
            }
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        updateStatus(download)
        //Maybe we don´t want to remove this download, for future tries
        fetch.remove(download.id)
        seriesMap[download.request.identifier]?.let { serie ->
            removeFile(serie)
            val message = error.throwable?.message
                          ?: resourceProvider.getString(R.string.error_general)
            logger.d(TAG, "onError: ${download.id} -- ${serie.title}")
            callbacks.iterator().forEach { callback ->
                callback.onError(SerieDownloaded(
                        serie = serie,
                        state = download.status.name,
                        error = message,
                        progress = download.progress.toPercentage()
                ))
            }
        }
    }

    override fun onWaitingNetwork(download: Download) {
        updateStatus(download)
    }

    //endregion

    private fun updateStatus(download: Download) {
        logger.d(TAG, "${download.id} is ${download.status} Progress: ${download.progress.toPercentage()}")
    }

    //region Callbacks

    fun addToCallbacks(callback: DownloadFileUseCaseListener) {
        if (!callbacks.contains(callback)) {
            logger.d(TAG, "Adding Callback: $callback}")
            callbacks.add(callback)
        }
    }

    fun removeCallbacks(callback: DownloadFileUseCaseListener) {
//        FIXME: Concurrent Modification exceptioon
//        val iterator = callbacks.iterator()
//        while (iterator.hasNext()) {
//            val cb = iterator.next()
//            if (cb == callback) {
//                iterator.remove()
//                logger.d(TAG, "Removing Callback: $callback}")
//            }
//        }
    }

    //endregion

    fun download(serieToDownload: Serie) {
        val file = getFile(serieToDownload)
        val request = Request(serieToDownload.downloadUrl, file.absolutePath).apply {
            downloadOnEnqueue = true
            priority = NORMAL
            networkType = WIFI_ONLY
            identifier = serieToDownload.id

        }
        seriesMap[request.identifier] = serieToDownload

        fetch.enqueue(request, Func {
            logger.d(TAG, "Request Enqueued ${it.id} - ${it.identifier}")
        }, Func {
            logger.d(TAG, "Request Error: ${it.throwable?.message}")
        })
    }

    fun pause(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            logger.d(TAG, "invokePaused: ${downloadList.map { it.id }}")
            fetch.pause(downloadList.map { it.id })
        })

    }

    fun resume(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
            fetch.resume(downloadList.map { it.id })
        })
    }

    fun cancel(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
            fetch.delete(downloadList.map { it.id })
        })
    }

    //region Files

    private fun removeFile(serie: Serie) {
        val file = getFile(serie)
        val deleted = file.delete()
        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
    }

    private fun getFile(serie: Serie): File {
        val storageDir = File(Environment.getExternalStoragePublicDirectory(PATH), DIRECTORY).apply { mkdirs() }
        return File(storageDir, "${serie.title}.mp4")
    }

    //endregion

    interface DownloadFileUseCaseListener {
        fun onSuccess(serieDownloaded: SerieDownloaded) = Unit
        fun onError(serieDownloaded: SerieDownloaded) = Unit
        fun onQueued(serieDownloaded: SerieDownloaded) = Unit
        fun onProgress(serieDownloaded: SerieDownloaded) = Unit
        fun onPaused(serieDownloaded: SerieDownloaded) = Unit
        fun onResumed(serieDownloaded: SerieDownloaded) = Unit
        fun onDeleted(serieDownloaded: SerieDownloaded) = Unit
    }

}
