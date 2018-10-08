package com.android.es.roversanz.series.presentation.di.module

import com.android.es.roversanz.series.presentation.ui.list.FactorySeriesListViewModel
import com.android.es.roversanz.series.usecases.series.GetSeriesListUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    internal fun provideFactorySeriesListViewModel(useCase: GetSeriesListUseCase) = FactorySeriesListViewModel(useCase)

}