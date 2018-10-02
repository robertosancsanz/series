package com.android.es.roversanz.series.data.provider

import io.reactivex.Scheduler

interface SchedulersProvider {

    fun backgroundThread(): Scheduler

    fun uiThread(): Scheduler

}