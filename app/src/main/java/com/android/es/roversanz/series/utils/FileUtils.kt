package com.android.es.roversanz.series.utils

import android.content.Context
import com.android.es.roversanz.series.utils.logger.Logger
import java.io.File

class FileUtil(private val context: Context,
               private val logger: Logger,
               val path: String) {

    companion object {
        private const val TAG: String = "DOWNLOADED"
        private const val DIRECTORY = "Series/"
    }

//    fun createFile(serie: Serie): File {
//        return createFile(serie.title)
//    }

    fun createFile(title: String): File {
        val storageDir = File(path, DIRECTORY).apply { mkdirs() }
        return File(storageDir, "$title.mp4")
    }

//    fun removeFile(serie: Serie) {
//        val file = createFile(serie)
//        val deleted = file.delete()
//        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
//    }

    fun removeFile(title: String) {
        val file = createFile(title)
        val deleted = file.delete()
        logger.d(TAG, "Removing File: ${file.absoluteFile} $deleted")
    }

    fun updateFolder(path: String?) {
        path?.let { context.updateMedia(it) }
    }

}
