package com.pragament.mlapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "image_results")
data class ImageResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val modelName: String,
    val label: String,
    val confidence: Float
)

