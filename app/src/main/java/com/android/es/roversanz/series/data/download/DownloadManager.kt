package com.android.es.roversanz.series.data.download

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.es.roversanz.series.R.string
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.COMPLETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.DELETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.ERROR
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.IDLE
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PAUSED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PROGRESS
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.QUEUED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.RESUMED
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.series.SerieDownloaded
import com.android.es.roversanz.series.utils.FileUtil
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
import com.tonyodev.fetch2core.Extras
import com.tonyodev.fetch2core.Func

@Suppress("LargeClass")
class DownloadManager(
        private val fileUtil: FileUtil,
        private val logger: Logger,
        private val fetch: Fetch,
        private val resourceProvider: ResourceProvider) : FetchListener {

    companion object {
        private const val TAG: String = "DOWNLOADED"
    }

    private val seriesMap = mutableMapOf<Long, Int>()
    private val serieLock = Any()

    private val _state = MutableLiveData<DownloadManagerState>().apply {
        value = IDLE
    }
    val state: LiveData<DownloadManagerState>
        get() = _state

    init {
        fetch.addListener(this)
    }

    //region Operations

    fun download(serie: Serie) {
        if (!seriesMap.contains(serie.id.toLong())) {
            fetch.getDownloadsByRequestIdentifier(serie.id.toLong(), Func { downloadList ->
                if (downloadList.isNotEmpty()) {
                    logger.d(TAG, "invokeRemoving: ${downloadList.map { it.id }}")
                    fetch.remove(downloadList.map { it.id }, Func {
                        logger.d(TAG, "Requests Removed ${it.map { down -> down.request.id }}")
                        downLoadSerie(serie)
                    }, Func {
                        logger.d(TAG, "Failed to remove Requests ${it.throwable?.message}")
                        downLoadSerie(serie)
                    })
                } else {
                    downLoadSerie(serie)

                }
            })
        } else {
            Log.d(TAG, "Serie ${serie.title} is already downloading")
        }
    }

//    fun download(serieToDownload: Serie) {
//
//        if (!seriesMap.contains(serieToDownload.id.toLong())) {
//            fetch.getDownloadsByRequestIdentifier(serieToDownload.id.toLong(), Func { downloadList ->
//
//                if (downloadList.isNotEmpty()) {
//                    logger.d(TAG, "invokeRemoving: ${downloadList.map { it.id }}")
//                    fetch.remove(downloadList.map { it.id }, Func {
//                        logger.d(TAG, "Requests Removed ${it.map { down -> down.request.id }}")
//                        downLoadSerie(serieToDownload)
//                    }, Func {
//                        logger.d(TAG, "Failed to remove Requests ${it.throwable?.message}")
//                        downLoadSerie(serieToDownload)
//                    })
//                } else {
//                    downLoadSerie(serieToDownload)
//                }
//            })
//        } else {
//            Log.d(TAG, "Serie ${serieToDownload.title} is already downloading")
//        }
//    }

    fun pause(serieId: Int) {
        fetch.getDownloadsByRequestIdentifier(serieId.toLong(), Func { downloadList ->
            if (downloadList.isNotEmpty()) {
                logger.d(TAG, "invokePaused: ${downloadList.map { it.id }}")
                fetch.pause(downloadList.map { it.id })
            }
        })

    }

    fun resume(serieId: Int) {
        fetch.getDownloadsByRequestIdentifier(serieId.toLong(), Func { downloadList ->
            if (downloadList.isNotEmpty()) {
                logger.d(TAG, "invokeResume: ${downloadList.map { it.id }}")
                fetch.resume(downloadList.map { it.id })
            }
        })
    }

    fun cancel(serieId: Int) {
        fetch.getDownloadsByRequestIdentifier(serieId.toLong(), Func { downloadList ->
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
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            _state.postValue(QUEUED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
        updateStatus(download)
    }

    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            _state.postValue(PROGRESS(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

    }

    override fun onPaused(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            _state.postValue(PAUSED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onResumed(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            _state.postValue(RESUMED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage())))
        }
    }

    override fun onCompleted(download: Download) {
        updateStatus(download)
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            val file = download.request.extras.map["title"]?.let { fileUtil.createFile(it) }
            fileUtil.updateFolder(file?.path)
            logger.d(TAG, "onCompleted: ${download.id}")
            _state.postValue(COMPLETED(
                    SerieDownloaded(serie = serie, state = download.status.name, progress = download.progress.toPercentage(), filePath = file?.absolutePath)))
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
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            fileUtil.removeFile(serie.title)
            _state.postValue(DELETED(SerieDownloaded(serie = serie, state = download.status.name)))
            removeSerie(download.request.identifier)
        }
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        updateStatus(download)
        //Maybe we don´t want to remove this download, for future tries
        fetch.remove(download.id)
        getSerie(download.request.identifier)?.let { serieId ->
            val serie = Serie(
                    id = serieId,
                    title = download.extras.map[DownloadService.FIELD_TITLE].orEmpty(),
                    subtitle = download.extras.map[DownloadService.FIELD_SUBTITLE].orEmpty(),
                    description = download.extras.map[DownloadService.FIELD_DESCRIPTION].orEmpty(),
                    picture = download.extras.map[DownloadService.FIELD_PICTURE].orEmpty(),
                    downloadUrl = download.extras.map[DownloadService.FIELD_URL].orEmpty()
            )
            fileUtil.removeFile(serie.title)
            val message = error.throwable?.message
                          ?: resourceProvider.getString(string.error_general)
            logger.d(TAG, "onError: ${download.id}")
            _state.postValue(ERROR(
                    SerieDownloaded(serie = serie, state = download.status.name, error = message,
                                    progress = download.progress.toPercentage())))
            removeSerie(download.request.identifier)
        }
    }

    override fun onWaitingNetwork(download: Download) {
        updateStatus(download)
    }

    //endregion

    //region Series

    private fun addSerie(identifier: Long, serieId: Int) {
        synchronized(serieLock) {
            seriesMap[identifier] = serieId
        }
    }

//    private fun addSerie(identifier: Long, serie: Serie) {
//        synchronized(serieLock) {
//            seriesMap[identifier] = serie
//        }
//    }

    private fun getSerie(identifier: Long): Int? {
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

    private fun updateStatus(download: Download) {
        logger.d(TAG, "${download.id} is ${download.status} Progress: ${download.progress.toPercentage()}")
    }

    private fun downLoadSerie(serie: Serie) {
        val file = fileUtil.createFile(serie.title)
        val request = Request(serie.downloadUrl, file.absolutePath).apply {
            downloadOnEnqueue = true
            priority = NORMAL
            networkType = WIFI_ONLY
            identifier = serie.id.toLong()
            extras = Extras(mutableMapOf<String, String>().apply {
                put(DownloadService.FIELD_TITLE, serie.title)
                put(DownloadService.FIELD_SUBTITLE, serie.subtitle)
                put(DownloadService.FIELD_DESCRIPTION, serie.description)
                put(DownloadService.FIELD_PICTURE, serie.picture)
                put(DownloadService.FIELD_URL, serie.downloadUrl)

            })
        }

        addSerie(request.identifier, serie.id)


        fetch.enqueue(request)

    }

//    private fun downLoadSerie(serieToDownload: Serie) {
//        val file = fileUtil.createFile(serieToDownload)
//        val request = Request(serieToDownload.downloadUrl, file.absolutePath).apply {
//            downloadOnEnqueue = true
//            priority = NORMAL
//            networkType = WIFI_ONLY
//            identifier = serieToDownload.id.toLong()
//        }
//        addSerie(request.identifier, serieToDownload)
//
//        fetch.enqueue(request)
//    }

    sealed class DownloadManagerState {

        object IDLE : DownloadManagerState()

        class QUEUED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class PROGRESS(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class PAUSED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class RESUMED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class COMPLETED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class DELETED(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

        class ERROR(val serieDownloaded: SerieDownloaded) : DownloadManagerState()

    }

}


