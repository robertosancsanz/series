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
        private val resourceProvider: ResourceProvider) : UseCase {

    companion object {
        private val TAG: String = "DOWNLOADED"
        private val PATH: String = Environment.DIRECTORY_DOWNLOADS
    }


    val map = mutableMapOf<Long, Int>()

    @Suppress("ComplexMethod")
    operator fun invoke(
            serie: Serie,
            onSuccess: (SerieDownloaded) -> Unit,
            onError: ((SerieDownloaded) -> Unit)? = null,
            onQueued: ((SerieDownloaded) -> Unit)? = null,
            onProgress: ((SerieDownloaded) -> Unit)? = null,
            onPaused: ((SerieDownloaded) -> Unit)? = null,
            onResumed: ((SerieDownloaded) -> Unit)? = null,
            onDeleted: ((SerieDownloaded) -> Unit)? = null) {

        val storageDir = File(Environment.getExternalStoragePublicDirectory(PATH), "Series/")
                .apply {
                    logger.d(TAG, "Creating directory: " + mkdirs())
                }
        val file = File.createTempFile(serie.title, ".mp4", storageDir)

        val listener = object : FetchListener {

            override fun onAdded(download: Download) {
//                logger.d(TAG, "onAdded: ${download.id}")
                updateStatus(download)
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
//                logger.d(TAG, "onQueued: ${download.id} $waitingOnNetwork")
                updateStatus(download)
                onQueued?.invoke(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
//                logger.d(TAG, "onStarted: ${download.id} -- $totalBlocks")
                updateStatus(download)
                map[serie.id] = download.id
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
//                logger.d(TAG, "onProgress: ${download.id} -- ${download.progress} " +
//                              "-- Speed:  $downloadedBytesPerSecond -- $etaInMilliSeconds ")
                updateStatus(download)
                onProgress?.invoke(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
//                logger.d(TAG, "onDownloadBlockUpdated: ${download.id} -- ${downloadBlock
//                        .downloadedBytes}")
            }

            override fun onPaused(download: Download) {
//                logger.d(TAG, "onPaused: ${download.id}")
                onPaused?.invoke(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }

            override fun onResumed(download: Download) {
//                logger.d(TAG, "onResumed: ${download.id}")
                onResumed?.invoke(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage()))
            }

            override fun onCompleted(download: Download) {
//                logger.d(TAG, "onCompleted: ${download.id} -- ${download.downloaded}")
                updateStatus(download)
                downloadManager.removeListener(this)
                onSuccess.invoke(SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage(), filePath = file.absolutePath))
            }

            override fun onCancelled(download: Download) {
//                logger.d(TAG, "onCancelled: ${download.id}")
                file.delete()
                downloadManager.removeListener(this)
            }

            override fun onRemoved(download: Download) {
//                logger.d(TAG, "onRemoved: ${download.id}")
                file.delete()
                downloadManager.removeListener(this)
            }

            override fun onDeleted(download: Download) {
                logger.d(TAG, "onDeleted: ${download.id}")
                file.delete()
                downloadManager.removeListener(this)
                onDeleted?.invoke(SerieDownloaded(serie = serie, state = download.status.name))
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                logger.d(TAG, "onError: ${download.id}")
                file.delete()
                downloadManager.removeListener(this)
                val message = error.throwable?.message
                              ?: resourceProvider.getString(R.string.error_general)
                onError?.invoke(SerieDownloaded(serie = serie, state = download.status.name, error = message, progress = download.progress.toPercentage()))
            }

            override fun onWaitingNetwork(download: Download) {
                logger.d(TAG, "onWaitingNetwork: ${download.id}")
            }

        }
        val request = Request(serie.downloadUrl, file.absolutePath).apply {
            downloadOnEnqueue = true
            priority = NORMAL
            networkType = WIFI_ONLY

        }

        downloadManager.addListener(listener)
        downloadManager.enqueue(request, Func {
            //            logger.d(TAG, "Request Enqueued")
        }, Func {
            logger.d(TAG, "Request Error: ${it.throwable?.message}")
        })

    }

    fun invokePaused(serie: Serie) {
        map[serie.id]?.let { downloadManager.pause(it) }
    }

    fun invokeResume(serie: Serie) {
        map[serie.id]?.let { downloadManager.resume(it) }
    }

    fun invokeCancel(serie: Serie) {
        map[serie.id]?.let { downloadManager.delete(it) }
    }

    private fun updateStatus(download: Download) {
        logger.d(TAG, "${download.id} is ${download.status} Progress: ${download.progress.toPercentage()}")
    }

}
