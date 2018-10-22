package com.android.es.roversanz.series.data.download

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.android.es.roversanz.series.BuildConfig
import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.DELETED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PAUSED
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.PROGRESS
import com.android.es.roversanz.series.data.download.DownloadManager.DownloadManagerState.RESUMED
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.presentation.MyApplication
import com.android.es.roversanz.series.presentation.di.components.MainComponent
import com.android.es.roversanz.series.presentation.di.scopes.ServiceScope
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.toIntPercentage
import dagger.Component
import java.security.InvalidParameterException
import javax.inject.Inject
import javax.inject.Named

class DownloadService : JobService() {

    companion object {
        val TAG: String = DownloadService::class.java.simpleName
        const val NOTIFICATION_GROUP = "DOWNLOAD"

        const val FIELD_ID = "id"
        const val FIELD_TITLE = "title"
        const val FIELD_SUBTITLE = "subtitle"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PICTURE = "picture"
        const val FIELD_URL = "url"
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var notificationManager: NotificationManager

    private var params: JobParameters? = null
    private val observer = Observer<DownloadManagerState> {
        it?.let { status ->
            logger.d(TAG, "Status: $status")

            notificationManager.notify(applicationContext, status, BuildConfig.CHANNEL_ID, NOTIFICATION_GROUP)

            if (status is DownloadManagerState.COMPLETED
                || status is DownloadManagerState.ERROR
                || status is DownloadManagerState.DELETED) {

                params?.let { jobFinished(it, false) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        inject(application as MyApplication)

        startObserve()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopObserve()
    }

    //region Job

    override fun onStartJob(params: JobParameters?): Boolean {
        doSampleJob(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        logger.d(TAG, "onStopJob")
        val id = params?.extras?.getInt("id")
        id?.let { downloadManager.cancel(it) }
        return false
    }

    private fun doSampleJob(params: JobParameters?) {
        this.params = params
        params?.extras?.let { bundle ->
            val serie = Serie(
                    id = bundle.getInt(FIELD_ID),
                    title = bundle.getString(FIELD_TITLE).orEmpty(),
                    subtitle = bundle.getString(FIELD_SUBTITLE).orEmpty(),
                    picture = bundle.getString(FIELD_PICTURE).orEmpty(),
                    description = bundle.getString(FIELD_DESCRIPTION).orEmpty(),
                    downloadUrl = bundle.getString(FIELD_URL).orEmpty()
            )
            logger.d(TAG, "Serie ${serie.id}: ${serie.title}")
            downloadManager.download(serie)

        } ?: throw InvalidParameterException("It´s needed send all the parameters")
    }

    //endregion

    //region Observers

    private fun startObserve() {
        downloadManager.state.observeForever(observer)
    }

    private fun stopObserve() {
        downloadManager.state.removeObserver { observer }
    }

    //endregion

    //region Notification

    private fun NotificationManager.notify(context: Context,
                                           status: DownloadManagerState,
                                           channel: String,
                                           notificationGroup: String) {

        val serie = status.serieDownloaded.serie
        val builder = NotificationCompat.Builder(context, channel)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(serie.subtitle)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_PROMO)
                .setGroup(notificationGroup)
                .setOnlyAlertOnce(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
        }
        if (status is PROGRESS) {
            val pauseIntent = Intent(context, DownloadBroadcastReceiver::class.java).apply {
                action = PAUSED::class.java.canonicalName
                putExtra(FIELD_ID, serie.id)
            }
            val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0)

            builder.setContentTitle("${serie.title} is ${status.serieDownloaded.state} ${status.serieDownloaded.progress}")
                    .setProgress(100, status.serieDownloaded.progress.toIntPercentage(), false)
                    .addAction(R.mipmap.ic_launcher_round, getString(R.string.button_pause),
                               pausePendingIntent)
        } else {
            builder.setContentTitle("${serie.title} is ${status.serieDownloaded.state}")
            if (status is PAUSED) {
                val resumeIntent = Intent(context, DownloadBroadcastReceiver::class.java).apply {
                    action = RESUMED::class.java.canonicalName
                    putExtra(FIELD_ID, serie.id)
                }
                val resumePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, resumeIntent, 0)

                builder.addAction(R.mipmap.ic_launcher_round, getString(R.string.button_resume), resumePendingIntent)

                val cancelIntent = Intent(context, DownloadBroadcastReceiver::class.java).apply {
                    action = DELETED::class.java.canonicalName
                    putExtra(FIELD_ID, serie.id)
                }
                val cancelPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0)

                builder.addAction(R.mipmap.ic_launcher_round, getString(R.string.button_cancel), cancelPendingIntent)
            }
        }


        this.notify(serie.id, builder.build())
    }

    //endregion

    //region di

    private fun inject(app: MyApplication) {
        DaggerDownloadService_DownloadServiceComponent.builder()
                .mainComponent(app.component)
                .build()
                .inject(this)
    }

    @Component(dependencies = [(MainComponent::class)])
    @ServiceScope
    internal interface DownloadServiceComponent {

        fun inject(service: DownloadService)
    }

    //endregion

}




