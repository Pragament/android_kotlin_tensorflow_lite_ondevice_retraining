package com.pragament.mlapp

import android.app.Application
import androidx.lifecycle.LiveData
import com.pragament.mlapp.database.AppDatabase
import com.pragament.mlapp.database.ImageResult
import com.pragament.mlapp.database.ImageResultDao

class ImageRepository(application: Application) {

    private val imageResultDao: ImageResultDao = AppDatabase.getInstance(application).imageResultDao()

    suspend fun getAllResults(): LiveData<List<ImageResult>> {
        return imageResultDao.getAllResults()
    }

    suspend fun getFilteredResults(minConfidence: Float): List<ImageResult> {
        return imageResultDao.getFilteredResults(minConfidence)
    }

    suspend fun getTopNResults(topN: Int): List<ImageResult> {
        return imageResultDao.getTopNResults(topN)
    }

    suspend fun getResultsByModel(modelName: String): List<ImageResult> {
        return imageResultDao.getResultsByModel(modelName)
    }

    suspend fun getResultsByLabel(label: String): List<ImageResult> {
        return imageResultDao.getResultsByLabel(label)
    }
}
