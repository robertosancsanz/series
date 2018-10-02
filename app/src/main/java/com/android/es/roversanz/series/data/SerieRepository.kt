package com.android.es.roversanz.series.data

import com.android.es.roversanz.series.data.provider.SchedulersProvider
import com.android.es.roversanz.series.domain.Serie
import io.reactivex.Single

interface SerieRepository {

    fun getSeries(
//            onSuccess: (List<Serie>) -> Unit, onError: ((String) -> Unit)?
    ): Single<List<Serie>>

    fun getSerie(id: Long
//                 ,onSuccess: (Serie) -> Unit, onError: ((String) -> Unit)?
    ): Single<Serie?>

}

class SerieRepositoryImpl(private val schedulers: SchedulersProvider,
                          private val localDataPersistence: DataPersistence) : SerieRepository {


    companion object {
        private val TAG = SerieRepositoryImpl::class.java.simpleName
    }

    override fun getSeries(): Single<List<Serie>> = Single.just(localDataPersistence.fetchSeries())
            .observeOn(schedulers.backgroundThread())
            .subscribeOn(schedulers.uiThread())
//                .subscribe(
//                        { list -> onSuccess.invoke(list) },
//                        { e ->
//                            onError?.invoke(
//                                    e?.message?.let { it }
//                                            ?: resourceProvider.getString(R.string.error_general)
//                            )
//                        }
//                )

    override fun getSerie(id: Long): Single<Serie?> = Single.just(localDataPersistence.fetchSeriesById(id))
            .observeOn(schedulers.backgroundThread())
            .subscribeOn(schedulers.uiThread())
//                .subscribe(
//                        { series ->
//                            series.firstOrNull { it.id == id }
//                                    ?.let(onSuccess)
//                                    ?: onError?.invoke(resourceProvider.getString(R.string.error_general))
//                        },
//                        { e ->
//                            onError?.invoke(
//                                    e?.message?.let { it }
//                                            ?: resourceProvider.getString(R.string.error_general)
//                            )
//                        })

}
