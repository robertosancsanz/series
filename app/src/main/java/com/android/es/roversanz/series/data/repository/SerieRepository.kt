package com.android.es.roversanz.series.data.repository

import com.android.es.roversanz.series.R
import com.android.es.roversanz.series.data.model.Serie
import com.android.es.roversanz.series.di.MySchedulers
import com.android.es.roversanz.series.utils.ResourceProvider
import com.android.es.roversanz.series.utils.logger.Logger
import io.reactivex.Observable

interface SerieRepository {

    fun getSeries(onSuccess: (List<Serie>) -> Unit,
                  onError: ((String) -> Unit)?)

    fun getSerie(id: Long,
                 onSuccess: (Serie) -> Unit,
                 onError: ((String) -> Unit)?)

}

class SerieRepositoryImpl(private val schedulers: MySchedulers,
                          private val logger: Logger,
                          private val resourceProvider: ResourceProvider) :
        SerieRepository {

    private val series: List<Serie> by lazy {
        listOf(
                Serie(1, "El Cuento de la Criada", "Muy heavy"),
                Serie(2, "Picky Blinders ", "Son mu malos"))
    }

    companion object {
        private val TAG = SerieRepositoryImpl::class.java.simpleName
    }

    override fun getSeries(onSuccess: (List<Serie>) -> Unit,
                           onError: ((String) -> Unit)?) {

        Observable.just(series)
                .observeOn(schedulers.backgroundThread())
                .subscribeOn(schedulers.uiThread())
                .subscribe(
                        { list -> onSuccess.invoke(list) },
                        { e ->
                            onError?.invoke(
                                    e?.message?.let { it }
                                            ?: resourceProvider.getString(R.string.error_general)
                            )
                        }
                )
    }

    override fun getSerie(id: Long,
                          onSuccess: (Serie) -> Unit,
                          onError: ((String) -> Unit)?) {

        Observable.just(series)
                .observeOn(schedulers.backgroundThread())
                .subscribeOn(schedulers.uiThread())
                .subscribe(
                        { series ->
                            series.firstOrNull { it.id == id }
                                    ?.let(onSuccess)
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
