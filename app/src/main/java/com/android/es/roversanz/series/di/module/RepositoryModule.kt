package com.android.es.roversanz.series.di.module

import com.android.es.roversanz.series.data.repository.SerieRepository
import com.android.es.roversanz.series.data.repository.SerieRepositoryImpl
import com.android.es.roversanz.series.di.MySchedulers
import com.android.es.roversanz.series.di.SchedulersImpl
import com.android.es.roversanz.series.utils.ResourceProvider
import com.android.es.roversanz.series.utils.logger.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    internal fun provideSchedulers(): MySchedulers = SchedulersImpl()

    @Provides
    @Singleton
    internal fun provideSerieRepository(schedulers: MySchedulers,
                                        logger: Logger,
                                        resourceProvider: ResourceProvider):
            SerieRepository = SerieRepositoryImpl(schedulers, logger, resourceProvider)

}
