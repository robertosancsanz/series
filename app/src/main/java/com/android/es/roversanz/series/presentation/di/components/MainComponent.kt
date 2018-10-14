package com.android.es.roversanz.series.presentation.di.components

import android.content.Context
import com.android.es.roversanz.series.data.DownloadManager
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.di.module.LoggerModule
import com.android.es.roversanz.series.presentation.di.module.ApplicationModule
import com.android.es.roversanz.series.presentation.di.module.RepositoryModule
import com.android.es.roversanz.series.presentation.di.module.UseCasesModule
import com.android.es.roversanz.series.usecases.download.CancelDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.DownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.PauseDownloadFileUseCase
import com.android.es.roversanz.series.usecases.download.ResumeDownloadFileUseCase
import com.android.es.roversanz.series.usecases.series.GetSerieDetailUseCase
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, LoggerModule::class, RepositoryModule::class,
    UseCasesModule::class])
interface MainComponent {

    fun provideContext(): Context

    fun provideResourceProvider(): ResourceProvider

    fun provideLogger(): Logger

    fun provideSerieRepository(): SerieRepository

    fun provideDownloadManager(): DownloadManager

    fun provideGetSeriesListUseCase(): GetSeriesListUseCase

    fun provideGetSerieDetailUseCase(): GetSerieDetailUseCase

    fun provideDownloadFileUseCase(): DownloadFileUseCase

    fun providePauseDownloadFileUseCase(): PauseDownloadFileUseCase

    fun provideResumeDownloadFileUseCase(): ResumeDownloadFileUseCase

    fun provideCancelDownloadFileUseCase(): CancelDownloadFileUseCase

}
