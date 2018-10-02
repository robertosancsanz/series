package com.android.es.roversanz.series.presentation.di.components

import android.content.Context
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.data.provider.SchedulersProvider
import com.android.es.roversanz.series.di.module.LoggerModule
import com.android.es.roversanz.series.presentation.di.module.ApplicationModule
import com.android.es.roversanz.series.presentation.di.module.RepositoryModule
import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, LoggerModule::class, RepositoryModule::class])
interface MainComponent {

    fun provideContext(): Context

    fun provideResourceProvider(): ResourceProvider

    fun provideLogger(): Logger

    fun provideSchedulers(): SchedulersProvider

    fun provideSerieRepository(): SerieRepository

}
