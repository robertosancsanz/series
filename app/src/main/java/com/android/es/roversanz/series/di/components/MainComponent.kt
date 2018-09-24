package com.android.es.roversanz.series.di.components

import android.content.Context
import com.android.es.roversanz.series.data.repository.SerieRepository
import com.android.es.roversanz.series.di.MySchedulers
import com.android.es.roversanz.series.di.module.ApplicationModule
import com.android.es.roversanz.series.di.module.LoggerModule
import com.android.es.roversanz.series.di.module.RepositoryModule
import com.android.es.roversanz.series.utils.ResourceProvider
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, LoggerModule::class, RepositoryModule::class])
interface MainComponent {

    fun provideContext(): Context

    fun provideResourceProvider(): ResourceProvider

    fun provideLogger(): Logger

    fun provideSchedulers(): MySchedulers

    fun provideSerieRepository(): SerieRepository

}
