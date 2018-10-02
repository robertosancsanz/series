package com.android.es.roversanz.series.presentation.di.module

import android.content.Context
import com.android.es.roversanz.series.utils.provider.ResourceProvider
import com.android.es.roversanz.series.utils.provider.ResourceProviderImpl
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
    internal fun provideResourceProvider(ctx: Context): ResourceProvider = ResourceProviderImpl(ctx)

}
