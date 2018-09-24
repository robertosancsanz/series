package com.android.es.roversanz.series.di.module

import android.content.Context
import com.android.es.roversanz.series.di.MySchedulers
import com.android.es.roversanz.series.di.SchedulersImpl
import com.android.es.roversanz.series.utils.ResourceProvider
import com.android.es.roversanz.series.utils.ResourceProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val ctx: Context) {

    @Provides
    @Singleton
    internal fun provideContext() = ctx

    @Provides
    @Singleton
    internal fun provideStringProvider(ctx: Context): ResourceProvider = ResourceProviderImpl(ctx)

    @Provides
    @Singleton
    internal fun provideSchedulers(): MySchedulers = SchedulersImpl()

}
