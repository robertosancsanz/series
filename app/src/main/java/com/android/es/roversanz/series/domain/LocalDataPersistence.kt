package com.android.es.roversanz.series.domain

import com.android.es.roversanz.series.data.DataPersistence

class LocalDataPersistence : DataPersistence {

    @Suppress("MaxLineLength")
    private val series: MutableList<Serie> by lazy {
        mutableListOf(
                Serie(1, "El Cuento de la Criada", "Muy heavy", "Esta serie dramática basada en " +
"el premiado best-seller de Margaret Atwood, narra la vida distópica de Gilead, una sociedad totalitaria que antiguamente pertenecía " +
"a los Estados Unidos. Los desastres medioambientales y una baja tasa de natalidad provocan que en Gilead gobierne un régimen " +
"fundamentalista perverso que considera a las mujeres propiedad del estado. Una de las últimas mujeres fértiles es Defred (Elisabeth Moss)," +
" sirvienta de la familia del líder y una de las mujeres forzadas a la esclavitud sexual para llevar a cabo un último intento desesperado " +
"de repoblar un mundo devastado. En esta horrible sociedad en la que una palabra inadecuada podría acabar con su vida, Defred se abre camino " +
"entre jefes, sus esposas crueles, mujeres del hogar y sus compañeras sirvientas –de las que cualquiera podría ser una espía de Gilead– con un " +
"único objetivo: sobrevivir y encontrar a la hija que le arrebataron.",
4.2,"https://www.ecestaticos.com/imagestatic/clipping/03b/c8f/03bc8fb84150aa179e63bc8898802516/imagen-sin-titulo.jpg?mtime=1531473691",
"https://www.noao.edu/image_gallery/images/d7/cygloop.jpg"                 ),
                Serie(2, "Picky Blinders ", "Son mu malos", "Serie de TV (2013-Actualidad). Una familia de " +
"gánsters asentada en Birmingham tras la Primera Guerra Mundial (1914-1918), dirige un local de apuestas hípicas. Las actividades del ambicioso " +
"jefe de la banda llaman la atención del Inspector jefe Chester Campbell, un detective de la Real Policía Irlandesa que es enviado desde Belfast " +
"para limpiar la ciudad y acabar con la banda.", 4.4,
"https://i.blogs.es/f8389e/espinof-critica-de-peaky-blinders-temporada-4/450_1000.jpg",
"https://www.noao.edu/image_gallery/images/d7/cygloop.jpg" ))
    }

    override fun fetchSeries(): List<Serie> = series

    override fun fetchSeriesById(id: Long): Serie? = series.firstOrNull { it.id == id }

    override fun addSerie(serie: Serie) {
        series.add(serie)
    }
}
