package com.android.es.roversanz.series.presentation.di.module

import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import com.android.es.roversanz.series.usecases.series.GetSerieUseCase
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
    internal fun provideSeriesListRepository(schedulers: SchedulersProvider,
                                        repository: SerieRepository,
                                        resourceProvider: ResourceProvider) = GetSeriesListUseCase(schedulers, repository, resourceProvider)

    @Provides
    @Singleton
    internal fun provideSerieRepository(schedulers: SchedulersProvider,
                                        repository: SerieRepository,
                                        resourceProvider: ResourceProvider) = GetSerieUseCase(schedulers, repository, resourceProvider)

}
