package com.android.es.roversanz.series.usecases.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.BroadcastScope
import com.android.es.roversanz.series.utils.app
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Component
import javax.inject.Inject

class DownloadReceiver : BroadcastReceiver() {

    companion object {
        private val TAG: String = "DOWNLOAD"
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onReceive(context: Context, intent: Intent) {
        inject(context.app())

        // get the refid from the download manager
        val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        logger.d(TAG, "Download received: $referenceId")

        val query = DownloadManager.Query().apply {
            setFilterById(referenceId)
        }

        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> logger.d(TAG, "Success!!: $referenceId")
                DownloadManager.STATUS_FAILED     ->
                    logger.d(TAG, "Error!!: $referenceId : ${cursor
                            .getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))}")
                DownloadManager.STATUS_PAUSED     -> logger.d(TAG, "Paused!!: $referenceId")
                else                              -> logger.d(TAG, "Status: $status: $referenceId")
            }
        }
        cursor.close()
    }

    private fun inject(app: MyApplication) {
        DaggerDownloadReceiver_DownloadReceiverComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @BroadcastScope
    @Component(dependencies = [(MainComponent::class)])
    internal interface DownloadReceiverComponent {

        fun inject(receiver: DownloadReceiver)
    }

}
