package com.pragament.mlapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pragament.mlapp.ImageResultDiffCallback
import com.pragament.mlapp.database.ImageResult
import com.pragament.mlapp.databinding.ItemResultBinding  // Import the correct binding class

class ImageResultAdapter : ListAdapter<ImageResult, ImageResultAdapter.ImageResultViewHolder>(ImageResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageResultViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageResultViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result)
    }

    inner class ImageResultViewHolder(private val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: ImageResult) {
            // Bind the data to the views in the item layout
            binding.imageView.setImageURI(Uri.parse(result.imagePath))  // Assuming you store the image path
            binding.labelTextView.text = result.label
            binding.confidenceTextView.text = "Confidence: ${result.confidence}"
            binding.modelTextView.text = result.modelName
        }
    }
}