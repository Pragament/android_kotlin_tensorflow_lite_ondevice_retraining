package com.pragament.mlapp

import androidx.recyclerview.widget.DiffUtil
import com.pragament.mlapp.database.ImageResult

class ImageResultDiffCallback : DiffUtil.ItemCallback<ImageResult>() {

    override fun areItemsTheSame(oldItem: ImageResult, newItem: ImageResult): Boolean {
        // Compare unique identifiers of the items (e.g., ID)
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ImageResult, newItem: ImageResult): Boolean {
        // Compare all contents of the items
        return oldItem == newItem
    }
}
