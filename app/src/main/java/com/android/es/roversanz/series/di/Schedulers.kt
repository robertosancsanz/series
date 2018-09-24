package com.android.es.roversanz.series.di

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io


interface MySchedulers {

    fun backgroundThread(): Scheduler

    fun uiThread(): Scheduler

}

class SchedulersImpl : MySchedulers {

    override fun backgroundThread() = io()

    override fun uiThread() = AndroidSchedulers.mainThread()

}
