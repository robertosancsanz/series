package com.android.es.roversanz.series.presentation.ui.main

import com.android.es.roversanz.series.domain.Serie

sealed class MainState {

    object INITIAL : MainState()

    object LIST : MainState()

    class DETAIL(val serie: Serie) : MainState()

}