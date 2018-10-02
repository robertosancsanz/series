package com.android.es.roversanz.series.usecases.provider

import io.reactivex.Scheduler

interface SchedulersProvider {

    fun backgroundThread(): Scheduler

    fun uiThread(): Scheduler

}
