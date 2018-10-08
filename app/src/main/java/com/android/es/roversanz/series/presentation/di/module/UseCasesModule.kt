package com.android.es.roversanz.series.presentation.di.module

import android.app.DownloadManager
import android.content.Context
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import com.android.es.roversanz.series.usecases.series.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSerieDetailUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.provider.SchedulersProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UseCasesModule {

    @Provides
    @Singleton
    internal fun provideSchedulers(): SchedulersProvider = SchedulersProviderImpl()

    @Provides
    @Singleton
    @Suppress("UnsafeCast")
    internal fun provideDownloadManager(ctx: Context): DownloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

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
    internal fun provideDownloadFileUseCase(downloadManager: DownloadManager) = DownloadFileUseCase(downloadManager)

}
