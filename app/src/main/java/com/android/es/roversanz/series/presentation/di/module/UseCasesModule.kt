package com.android.es.roversanz.series.presentation.di.module

import android.content.Context
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSerieDetailUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.provider.SchedulersProviderImpl
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
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
        private val CONNECT_TIMEOUT = 15000
        private val READ_TIMEOUT = 10000
        private val KEEP_ALIVE_TIME = 100
        private val THREAD_COUNT = 1
    }

    @Provides
    @Singleton
    internal fun provideSchedulers(): SchedulersProvider = SchedulersProviderImpl()

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectionPool(ConnectionPool(THREAD_COUNT, KEEP_ALIVE_TIME.toLong(), TimeUnit.MILLISECONDS))
        followRedirects(true)
        followSslRedirects(true)
        retryOnConnectionFailure(true)
        readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        //cookieJar(JavaNetCookieJar(CookieHandler.setDefault(CookieManager().apply {
        //  setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        //})))
        networkInterceptors().add(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
    }.build()

//    @Provides
//    @Singleton
//    @Suppress("UnsafeCast")
//    internal fun provideDownloadManager(ctx: Context): DownloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    @Provides
    @Singleton
    internal fun provideDownloadManager(ctx: Context, okHttpClient: OkHttpClient): Fetch {
        val fetchConfiguration = FetchConfiguration.Builder(ctx)
                .setDownloadConcurrentLimit(1)
                .setGlobalNetworkType(NetworkType.WIFI_ONLY)
                .setHttpDownloader(OkHttpDownloader(okHttpClient))
                .setNamespace("SERIES")
                .build()
        return Fetch.getInstance(fetchConfiguration)
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
    internal fun provideDownloadFileUseCase(logger: Logger,
                                            downloadManager: Fetch,
                                            resourceProvider: ResourceProvider) = DownloadFileUseCase(logger, downloadManager, resourceProvider)

}
