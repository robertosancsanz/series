package com.android.es.roversanz.series.usecases.series

import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.data.SerieRepository
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.usecases.UseCase
import com.android.es.roversanz.series.usecases.provider.SchedulersProvider
import com.android.es.roversanz.series.utils.provider.ResourceProvider

class GetSerieDetailUseCase(private val schedulers: SchedulersProvider,
                      private val repository: SerieRepository,
                      private val resourceProvider: ResourceProvider) : UseCase {

    operator fun invoke(id: Int, onSuccess: (Serie) -> Unit, onError: ((String) -> Unit)?) {
        repository.getSerie(id)
                .observeOn(schedulers.backgroundThread())
                .subscribeOn(schedulers.uiThread())
                .subscribe(
                        { serie ->
                            serie?.let(onSuccess)
                                    ?: onError?.invoke(resourceProvider.getString(R.string.error_general))
                        },
                        { e ->
                            onError?.invoke(
                                    e?.message?.let { it }
                                            ?: resourceProvider.getString(R.string.error_general)
                            )
                        })
    }

}
