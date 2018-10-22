package com.android.es.roversanz.series.presentation.di.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.android.es.roversanz.series.BuildConfig
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

    @Provides
    @Singleton
    @Suppress("UnsafeCast")
    internal fun provideNotificationManager(ctx: Context): NotificationManager =
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as (NotificationManager)).apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val mChannel = NotificationChannel(BuildConfig.CHANNEL_ID, BuildConfig.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                    this.createNotificationChannel(mChannel)
                }
            }

}
