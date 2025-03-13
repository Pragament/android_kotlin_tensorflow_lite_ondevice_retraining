package com.pragament.mlapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ImageResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: ImageResult)

    @Update
    suspend fun update(result: ImageResult)

    @Query("SELECT * FROM image_results")
     fun getAllResults(): LiveData<List<ImageResult>>

    @Query("SELECT * FROM image_results WHERE confidence >= :minConfidence")
    suspend fun getFilteredResults(minConfidence: Float): List<ImageResult>

    @Query("SELECT * FROM image_results ORDER BY confidence DESC LIMIT :topN")
    suspend fun getTopNResults(topN: Int): List<ImageResult>

    @Query("SELECT * FROM image_results WHERE modelName = :modelName")
    suspend fun getResultsByModel(modelName: String): List<ImageResult>

    @Query("SELECT * FROM image_results WHERE label LIKE :label")
    suspend fun getResultsByLabel(label: String): List<ImageResult>

    @Query("""
        SELECT * FROM image_results 
        WHERE confidence >= :minConfidence AND confidence <= :maxConfidence 
        AND (:label IS NULL OR label LIKE '%' || :label || '%')
        AND (:modelName IS NULL OR modelName = :modelName OR :modelName = 'none')
        ORDER BY 
        CASE WHEN :sortOption = 'asc' THEN confidence END ASC,
        CASE WHEN :sortOption = 'desc' THEN confidence END DESC
    """)
    suspend fun getFilteredResults(
        minConfidence: Float,
        maxConfidence: Float,
        label: String?,
        modelName: String?,
        sortOption: String
    ): List<ImageResult>
}
