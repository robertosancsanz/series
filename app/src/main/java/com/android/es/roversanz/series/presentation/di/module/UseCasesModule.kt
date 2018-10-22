package com.android.es.roversanz.series.presentation.di.module

import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Environment
import com.android.es.roversanz.series.data.download.DownloadManager
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.data.download.DownloadService
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import com.android.es.roversanz.series.usecases.series.GetSerieDetailUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.utils.FileUtil
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.provider.SchedulersProviderImpl
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Status.CANCELLED
import com.tonyodev.fetch2.Status.DELETED
import com.tonyodev.fetch2.Status.FAILED
import com.tonyodev.fetch2.Status.NONE
import com.tonyodev.fetch2.Status.REMOVED
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class UseCasesModule {

    companion object {
        private val CONNECT_TIMEOUT = 5
        private val READ_TIMEOUT = 3
        private val KEEP_ALIVE_TIME = 3
        private val THREAD_COUNT = 4
    }

    @Provides
    @Singleton
    internal fun provideSchedulers(): SchedulersProvider = SchedulersProviderImpl()

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectionPool(ConnectionPool(THREAD_COUNT, KEEP_ALIVE_TIME.toLong(), TimeUnit.SECONDS))
        followRedirects(true)
        followSslRedirects(true)
        retryOnConnectionFailure(true)
        readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        networkInterceptors().add(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
    }.build()

    @Provides
    @Singleton
    internal fun provideFileUtil(ctx: Context,
                                 logger: Logger): FileUtil
            = FileUtil(ctx, logger, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)

    @Provides
    @Singleton
    internal fun provideDownloadManager(
            ctx: Context,
            okHttpClient: OkHttpClient,
            logger: Logger,
            fileUtil: FileUtil,
            resourceProvider: ResourceProvider): DownloadManager {
        val fetchConfiguration = FetchConfiguration.Builder(ctx)
                .setDownloadConcurrentLimit(1)
                .setGlobalNetworkType(NetworkType.WIFI_ONLY)
                .setHttpDownloader(OkHttpDownloader(okHttpClient))
                .setProgressReportingInterval(500)
                .setNamespace("SERIES")
                .enableRetryOnNetworkGain(true)
                .build()
        val fetch = Fetch.getInstance(fetchConfiguration).apply {
            enableLogging(true)
            removeAllWithStatus(DELETED)
            removeAllWithStatus(CANCELLED)
            removeAllWithStatus(FAILED)
            removeAllWithStatus(NONE)
            removeAllWithStatus(REMOVED)
            removeAll()
        }

        return DownloadManager(fileUtil, logger, fetch, resourceProvider)
    }

    @Provides
    @Singleton
    internal fun provideGetSeriesListUseCase(schedulers: SchedulersProvider,
                                             repository: SerieRepository,
                                             resourceProvider: ResourceProvider) = GetSeriesListUseCase(schedulers, repository, resourceProvider)

    @Provides
    @Singleton
    internal fun provideGetSerieUseCase(schedulers: SchedulersProvider,
                                        repository: SerieRepository,
                                        resourceProvider: ResourceProvider) = GetSerieDetailUseCase(schedulers, repository, resourceProvider)

    @Provides
    @Singleton
    internal fun provideDownloadFileUseCase(ctx:Context,downloadManager: DownloadManager):
            DownloadFileUseCase {
        val jobScheduler = ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        val serviceComponent = ComponentName(ctx, DownloadService::class.java)
        return DownloadFileUseCase(downloadManager, jobScheduler, serviceComponent)
    }

    @Provides
    @Singleton
    internal fun providePauseDownloadFileUseCase(downloadManager: DownloadManager) = PauseDownloadFileUseCase(downloadManager)

    @Provides
    @Singleton
    internal fun provideResumeDownloadFileUseCase(downloadManager: DownloadManager) = ResumeDownloadFileUseCase(downloadManager)

    @Provides
    @Singleton
    internal fun provideCancelDownloadFileUseCase(downloadManager: DownloadManager) = CancelDownloadFileUseCase(downloadManager)

}
