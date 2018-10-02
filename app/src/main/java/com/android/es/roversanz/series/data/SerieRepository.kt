package com.android.es.roversanz.series.data

import com.android.es.roversanz.series.domain.Serie
import io.reactivex.Single

interface SerieRepository {

    fun getSeries(): Single<List<Serie>>

    fun getSerie(id: Long): Single<Serie?>

}

class SerieRepositoryImpl(private val localDataPersistence: DataPersistence) : SerieRepository {

    companion object {
        private val TAG = SerieRepositoryImpl::class.java.simpleName
    }

    override fun getSeries(): Single<List<Serie>> = Single.just(localDataPersistence.fetchSeries())

    override fun getSerie(id: Long): Single<Serie?> = Single.just(localDataPersistence.fetchSeriesById(id))

}
