package com.android.es.roversanz.series.usecases.series

import android.os.Environment
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
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

class DownloadFileUseCase(
        private val logger: Logger,
        private val downloadManager: Fetch,
        private val resourceProvider: ResourceProvider) : UseCase, FetchListener {

    companion object {
        private const val TAG: String = "DOWNLOADED"
        private val PATH: String = Environment.DIRECTORY_DOWNLOADS
        private const val DIRECTORY = "Series/"
    }

    private val seriesMap = mutableMapOf<Long, Serie>()
    private var callbacks = mutableListOf<DownloadFileUseCase.DownloadFileUseCaseListener>()

    init {
        downloadManager.addListener(this)
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
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.forEach { callback ->
                callback.onPaused(SerieDownloaded(serie = serie, state = download.status
                        .name, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onResumed(download: Download) {
        seriesMap[download.request.identifier]?.let { serie ->
            callbacks.forEach { callback ->
                callback.onResumed(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onCompleted(download: Download) {
        updateStatus(download)
        seriesMap[download.request.identifier]?.let { serie ->
            val filePath = Environment.getExternalStoragePublicDirectory(PATH).absolutePath + "Series/${serie.title}.mp4"
            callbacks.forEach { callback ->
                callback.onSuccess(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage(), filePath = filePath))
            }
        }
    }

    override fun onCancelled(download: Download) {
        logger.d(TAG, "onCancelled: ${download.id}")
//                file.delete()
        downloadManager.remove(download.id)
    }

    override fun onRemoved(download: Download) {
        logger.d(TAG, "onRemoved: ${download.id}")
//                file.delete()
    }

    override fun onDeleted(download: Download) {
        logger.d(TAG, "onDeleted: ${download.id}")
        downloadManager.remove(download.id)
        seriesMap[download.request.identifier]?.let { serie ->
            removeFile(serie)
            callbacks.forEach { callback ->
                callback.onDeleted(SerieDownloaded(serie = serie, state = download.status
                        .name))
            }
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        //Maybe we don´t want to remove this download, for future tries
        downloadManager.remove(download.id)
        seriesMap[download.request.identifier]?.let { serie ->
            removeFile(serie)
            val message = error.throwable?.message
                          ?: resourceProvider.getString(R.string.error_general)
            logger.d(TAG, "onError: ${download.id} -- ${serie.title}")
            callbacks.forEach { callback ->
                callback.onError(SerieDownloaded(serie = serie, state = download.status.name, error = message, progress = download.progress.toPercentage()))
            }
        }
    }

    override fun onWaitingNetwork(download: Download) {
        logger.d(TAG, "onWaitingNetwork: ${download.id}")
    }

    //endregion

    operator fun invoke(
            serieToDownload: Serie,
            callback: DownloadFileUseCaseListener) {

        val file = getFile(serieToDownload)
        val request = Request(serieToDownload.downloadUrl, file.absolutePath).apply {
            downloadOnEnqueue = true
            priority = NORMAL
            networkType = WIFI_ONLY
            identifier = serieToDownload.id

        }
        seriesMap[request.identifier] = serieToDownload

        if (!callbacks.contains(callback)) {
            this.callbacks.add(callback)
        }

        downloadManager.enqueue(request, Func {
            logger.d(TAG, "Request Enqueued ${it.id} - ${it.identifier}")
        }, Func {
            logger.d(TAG, "Request Error: ${it.throwable?.message}")
        })

    }

    fun invokePaused(serie: Serie) {
        downloadManager.getDownloadsByRequestIdentifier(serie.id, Func { downloadList ->
            logger.d(TAG, "invokePaused: ${downloadList.map { it.id }}")
            downloadManager.pause(downloadList.map { it.id })
        })
    }

    fun invokeResume(serie: Serie) {
        downloadManager.getDownloadsByRequestIdentifier(serie.id, Func { downloadList ->
            logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
            downloadManager.resume(downloadList.map { it.id })
        })
    }

    fun invokeCancel(serie: Serie) {
        downloadManager.getDownloadsByRequestIdentifier(serie.id, Func { downloadList ->
            logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
            downloadManager.delete(downloadList.map { it.id })
        })
    }

    private fun updateStatus(download: Download) {
        logger.d(TAG, "${download.id} is ${download.status} Progress: ${download.progress.toPercentage()}")
    }

    private fun removeFile(serie: Serie) {
        val file = getFile(serie)
        val deleted = file.delete()
        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
    }

    private fun getFile(serie: Serie): File {
        val storageDir = File(Environment.getExternalStoragePublicDirectory(PATH), DIRECTORY).apply { mkdirs() }
        return File(storageDir, "${serie.title}.mp4")
    }

    interface DownloadFileUseCaseListener {
        fun onSuccess(serie: SerieDownloaded) = Unit
        fun onError(serie: SerieDownloaded) = Unit
        fun onQueued(serie: SerieDownloaded) = Unit
        fun onProgress(serie: SerieDownloaded) = Unit
        fun onPaused(serie: SerieDownloaded) = Unit
        fun onResumed(serie: SerieDownloaded) = Unit
        fun onDeleted(serie: SerieDownloaded) = Unit
    }
}
