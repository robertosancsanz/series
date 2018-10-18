package com.android.es.roversanz.series.utils

import android.content.Context
import com.android.es.roversanz.series.domain.Serie
import com.android.es.roversanz.series.utils.logger.Logger
import java.io.File

class FileUtil(private val context: Context,
               private val logger: Logger,
               val path: String) {

    companion object {
        private const val TAG: String = "DOWNLOADED"
        private const val DIRECTORY = "Series/"
    }

    fun createFile(serie: Serie): File {
        val storageDir = File(path, DIRECTORY).apply { mkdirs() }
        return File(storageDir, "${serie.title}.mp4")
    }

    fun removeFile(serie: Serie) {
        val file = createFile(serie)
        val deleted = file.delete()
        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
    }

    fun updateFolder(path: String) {
        context.updateMedia(path)
    }

}
