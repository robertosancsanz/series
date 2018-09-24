package com.android.es.roversanz.series.di.module

import com.android.es.roversanz.series.utils.logger.Logger
import com.android.es.roversanz.series.utils.logger.LoggerBlanked
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LoggerModule {

    @Provides
    @Singleton
    internal fun provideLoggerWrapper(): Logger = LoggerBlanked()

}
