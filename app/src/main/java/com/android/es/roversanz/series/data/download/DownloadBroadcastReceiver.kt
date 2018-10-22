package com.android.es.roversanz.series.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.DELETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PAUSED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.RESUMED
import com.android.es.roversanz.series.data.download.DownloadService.Companion.FIELD_ID
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.BroadcastReceiverScope
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Component
import javax.inject.Inject

class DownloadBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = DownloadBroadcastReceiver::class.java.simpleName
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.app()?.let { inject(it) }
        intent?.let {
            when (it.action) {
                PAUSED::class.java.simpleName  -> it.extras?.getInt(FIELD_ID)?.let { serieId -> pause(serieId) }
                RESUMED::class.java.simpleName -> it.extras?.getInt(FIELD_ID)?.let { serieId -> resume(serieId) }
                DELETED::class.java.simpleName -> it.extras?.getInt(FIELD_ID)?.let { serieId -> cancel(serieId) }
                else                              -> logger.d(TAG, "Unkown action: ${intent.action}")
            }
        }
    }

    private fun resume(serieId: Int) {
        logger.d(TAG, "Resume $serieId")
        downloadManager.resume(serieId)
    }

    private fun pause(serieId: Int) {
        logger.d(TAG, "Pause $serieId")
        downloadManager.pause(serieId)
    }

    private fun cancel(serieId: Int) {
        logger.d(TAG, "Cancel $serieId")
        downloadManager.cancel(serieId)
    }

    //region di

    private fun inject(app: MyApplication) {

        DaggerDownloadBroadcastReceiver_DownloadBroadcastReceiverComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @Component(dependencies = [(MainComponent::class)])
    @BroadcastReceiverScope
    internal interface DownloadBroadcastReceiverComponent {

        fun inject(service: DownloadBroadcastReceiver)
    }

    //endregion
}
