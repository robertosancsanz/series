package com.android.es.roversanz.series.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
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

@Suppress("LargeClass")
class DownloadManager(
        private val context: Context,
        private val logger: Logger,
        private val fetch: Fetch,
        private val resourceProvider: ResourceProvider) : FetchListener {

    companion object {
        private const val TAG: String = "DOWNLOADED"
        private val PATH: String = Environment.DIRECTORY_DOWNLOADS
        private const val DIRECTORY = "Series/"
    }

    private val seriesMap = mutableMapOf<Long, Serie>()
    private val serieLock = Any()
//    private var callbacks = mutableListOf<WeakReference<DownloadFileUseCaseListener>>()
//    private val callbackLock = Any()

    init {
        fetch.addListener(this)
    }

    //region Operations

    fun download(serieToDownload: Serie) {

        if (!seriesMap.contains(serieToDownload.id)) {
            fetch.getDownloadsByRequestIdentifier(serieToDownload.id, Func { downloadList ->

                if (downloadList.isNotEmpty()) {
                    logger.d(TAG, "invokeRemoving: ${downloadList.map { it.id }}")
                    fetch.remove(downloadList.map { it.id }, Func {
                        logger.d(TAG, "Requests Removed ${it.map { down -> down.request.id }}")
                        downLoadSerie(serieToDownload)
                    }, Func {
                        logger.d(TAG, "Failed to remove Requests ${it.throwable?.message}")
                        downLoadSerie(serieToDownload)
                    })
                } else {
                    downLoadSerie(serieToDownload)
                }
            })
        } else {
            Log.d(TAG, "Serie ${serieToDownload.title} is already downloading")
        }
    }

    fun pause(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            if (downloadList.isNotEmpty()) {
                logger.d(TAG, "invokePaused: ${downloadList.map { it.id }}")
                fetch.pause(downloadList.map { it.id })
            }
        })

    }

    fun resume(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            if (downloadList.isNotEmpty()) {
                logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
                fetch.resume(downloadList.map { it.id })
            }
        })
    }

    fun cancel(serieId: Long) {
        fetch.getDownloadsByRequestIdentifier(serieId, Func { downloadList ->
            if (downloadList.isNotEmpty()) {
                logger.d(TAG, "invokeCancel: ${downloadList.map { it.id }}")
                fetch.delete(downloadList.map { it.id })
            }
        })
    }

    //endregion

    //region listener

    override fun onAdded(download: Download) {
        updateStatus(download)
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serie ->
            _state.postValue(DownloadManagerState.QUEUED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
        updateStatus(download)
    }

    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serie ->
            _state.postValue(DownloadManagerState.PROGRESS(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

    }

    override fun onPaused(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serie ->
            _state.postValue(DownloadManagerState.PAUSED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onResumed(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serie ->
            _state.postValue(DownloadManagerState.RESUMED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onCompleted(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serie ->
            val file = getFile(serie)
            logger.d(TAG, "onCompleted: ${download.id} -- ${serie.title}")
            updateFolder(file.path)
            _state.postValue(DownloadManagerState.COMPLETED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage(),
                                    filePath = file.absolutePath)))
            removeSerie(download.request.identifier)
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
        getSerie(download.request.identifier)?.let { serie ->
            removeFile(serie)
            _state.postValue(DownloadManagerState.DELETED(SerieDownloaded(serie = serie, state = download.status.name)))
            removeSerie(download.request.identifier)
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        updateStatus(download)
        //Maybe we don´t want to remove this download, for future tries
        fetch.remove(download.id)
        getSerie(download.request.identifier)?.let { serie ->
            removeFile(serie)
            val message = error.throwable?.message
                          ?: resourceProvider.getString(R.string.error_general)
            logger.d(TAG, "onError: ${download.id} -- ${serie.title}")
            _state.postValue(DownloadManagerState.ERROR(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
            removeSerie(download.request.identifier)
        }
    }

    override fun onWaitingNetwork(download: Download) {
        updateStatus(download)
    }

    //endregion

    //region Series

    private fun addSerie(identifier: Long, serie: Serie) {
        synchronized(serieLock) {
            seriesMap[identifier] = serie
        }
    }

    private fun getSerie(identifier: Long): Serie? {
        synchronized(serieLock) {
            return seriesMap[identifier]
        }
    }

    private fun removeSerie(identifier: Long) {
        synchronized(serieLock) {
            seriesMap.remove(identifier)
            logger.d(TAG, "Removing Serie")
        }
    }

    //endregion

    //region Callbacks

    private val _state = MutableLiveData<DownloadManagerState>().apply {
        value = DownloadManagerState.IDLE
    }

    val state: LiveData<DownloadManagerState>
        get() = _state

    //endregion

    //region Files
    //TODO: We can create a FileManager for handling this, instead of some methods here
    private fun removeFile(serie: Serie) {
        val file = getFile(serie)
        val deleted = file.delete()
        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
    }

    private fun getFile(serie: Serie): File {
        val storageDir = File(Environment.getExternalStoragePublicDirectory(PATH), DIRECTORY).apply { mkdirs() }
        return File(storageDir, "${serie.title}.mp4")
    }

    private fun updateFolder(path: String) {
        MediaScannerConnection.scanFile(context, Array<String>(1) { path }, null, null)
    }

    //endregion

    private fun updateStatus(download: Download) {
        logger.d(TAG, "${download.id} is ${download.status} Progress: ${download.progress.toPercentage()}")
    }

    private fun downLoadSerie(serieToDownload: Serie) {
        val file = getFile(serieToDownload)
        val request = Request(serieToDownload.downloadUrl, file.absolutePath).apply {
            downloadOnEnqueue = true
            priority = NORMAL
            networkType = WIFI_ONLY
            identifier = serieToDownload.id

        }
        addSerie(request.identifier, serieToDownload)

        fetch.enqueue(request)
    }

    interface DownloadFileUseCaseListener {
        fun onSuccess(serieDownloaded: SerieDownloaded) = Unit
        fun onError(serieDownloaded: SerieDownloaded) = Unit
        fun onQueued(serieDownloaded: SerieDownloaded) = Unit
        fun onProgress(serieDownloaded: SerieDownloaded) = Unit
        fun onPaused(serieDownloaded: SerieDownloaded) = Unit
        fun onResumed(serieDownloaded: SerieDownloaded) = Unit
        fun onDeleted(serieDownloaded: SerieDownloaded) = Unit
    }

    sealed class DownloadManagerState {

        object IDLE : DownloadManagerState()

        class QUEUED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class PROGRESS(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

        class PAUSED(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

        class RESUMED(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

        class COMPLETED(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

        class DELETED(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

        class ERROR(val serieDownloaded: SerieDownloaded) : DownloadManager.DownloadManagerState()

    }

}
