package com.android.es.roversanz.series.utils.provider

import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io

class SchedulersProviderImpl : SchedulersProvider {

    override fun backgroundThread() = io()

    override fun uiThread() = AndroidSchedulers.mainThread()

}
