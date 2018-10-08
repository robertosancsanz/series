package com.android.es.roversanz.series.usecases.series

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase

class DownloadFileUseCase(private val downloadManager: DownloadManager) : UseCase {

    companion object {
        private val TAG: String = "DOWNLOAD"
    }

    operator fun invoke(serie: Serie) {


        val request = DownloadManager.Request(Uri.parse(serie.downloadUrl)).apply {
            Log.d(TAG, "Path: " + Environment.DIRECTORY_DOWNLOADS + "/Series/${serie.title}.mp4")

            setTitle("Downloading ${serie.title}")
            setDescription("Downloading ${serie.title}")
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Series/${serie.title}.mp4")

            setNotificationVisibility(VISIBILITY_VISIBLE)
            setAllowedOverRoaming(false)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setVisibleInDownloadsUi(false)
        }

        val refid = downloadManager.enqueue(request)

        Log.d(TAG, "Download started: $refid")
    }

}
