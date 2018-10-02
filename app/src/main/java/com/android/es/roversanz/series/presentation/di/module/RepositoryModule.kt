package com.android.es.roversanz.series.presentation.di.module

import com.android.es.roversanz.series.data.DataPersistence
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.data.SerieRepositoryImpl
import com.android.es.roversanz.series.data.provider.SchedulersProvider
import com.android.es.roversanz.series.domain.LocalDataPersistence
import com.android.es.roversanz.series.utils.provider.SchedulersProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    internal fun provideSchedulers(): SchedulersProvider = SchedulersProviderImpl()

    @Provides
    @Singleton
    internal fun providelocalDataPersistence(): DataPersistence = LocalDataPersistence()

    @Provides
    @Singleton
    internal fun provideSerieRepository(schedulers: SchedulersProvider,
                                        persistence: DataPersistence):
            SerieRepository = SerieRepositoryImpl(schedulers, persistence)

}
