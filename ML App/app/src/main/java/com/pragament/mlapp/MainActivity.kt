package com.pragament.mlapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pragament.mlapp.adapters.ImageResultAdapter
import com.pragament.mlapp.database.ImageResult
import com.pragament.mlapp.viewModels.ImageViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var imageViewModel: ImageViewModel
    private lateinit var imageResultAdapter: ImageResultAdapter
    private lateinit var spinnerModel: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPickImages: Button
    private lateinit var imageClassifier: ImageClassifier
    private var currentModel: String = "mobilenet_v1"
    private lateinit var btnApplyFilters: Button
    private lateinit var editTextSearch: EditText
    private lateinit var editTextMinConfidence: EditText
    private lateinit var editTextMaxConfidence: EditText
    private lateinit var editTextTopN: EditText
    private lateinit var spinnerSortOptions: Spinner
    private lateinit var spinnerFilter: Spinner
    private lateinit var clearFilterButton: Button

    private lateinit var pickImagesLauncher: ActivityResultLauncher<String>
    private val imageResults = mutableListOf<ImageResult>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnPickImages = findViewById(R.id.btnPickImages)
        spinnerModel = findViewById(R.id.spinnerModel)
        btnApplyFilters = findViewById(R.id.btnApplyFilters)
        editTextSearch = findViewById(R.id.editTextSearch)
        editTextMinConfidence = findViewById(R.id.editTextMinConfidence)
        editTextMaxConfidence = findViewById(R.id.editTextMaxConfidence)
        editTextTopN = findViewById(R.id.editTextTopN)
        spinnerSortOptions = findViewById(R.id.spinnerSortOptions)
        spinnerFilter = findViewById(R.id.spinnerFilters)
        clearFilterButton = findViewById(R.id.clearFilterButton)

        imageResultAdapter = ImageResultAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = imageResultAdapter

        imageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]

        val models = arrayOf("mobilenet_v1", "mobilenet_v2")
        val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, models)
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModel.adapter = modelAdapter

        spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentModel = models[position]
                switchModel(currentModel)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val sortOptions = arrayOf("Confidence (High to Low)", "Confidence (Low to High)")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSortOptions.adapter = sortAdapter

        val filterOptions = models + arrayOf("None")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter


        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    handlePickedImages(uris)
                } else {
                    Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
                }
            }

        btnPickImages.setOnClickListener { pickImages() }

        btnApplyFilters.setOnClickListener { applyFilters() }

        clearFilterButton.setOnClickListener{clearFilters()}

        imageClassifier = ImageClassifier(this, "model1.tflite")

        imageViewModel.filteredResults.observe(this) { results ->
            imageResultAdapter.submitList(results.ifEmpty { imageViewModel.allResults.value ?: emptyList() })
        }

        imageViewModel.allResults.observe(this) { results ->
            if (imageViewModel.filteredResults.value.isNullOrEmpty()) {
                imageResultAdapter.submitList(results)
            }





        results.forEach { result ->
                try {
                    val uri = Uri.parse(result.imagePath)
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val persistedUris = contentResolver.persistedUriPermissions.map { it.uri }

                    if (!persistedUris.contains(uri)) {  // ✅ Check if permission is already granted
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    }
                } catch (e: SecurityException) {
                    Log.e("PermissionError", "Failed to restore URI permission: ${e.message}")
                }
            }
        }
    }

    private fun switchModel(model: String) {
        val modelPath = when (model) {
            "mobilenet_v1" -> "model1.tflite"
            "mobilenet_v2" -> "model2.tflite"
            else -> "model2.tflite"
        }
        imageClassifier.switchModel(modelPath)
    }

    private fun pickImages() {
        pickImagesLauncher.launch("image/*")
    }

    private fun handlePickedImages(imageUris: List<Uri>) {
        Toast.makeText(this, "${imageUris.size} images selected", Toast.LENGTH_SHORT).show()
        imageUris.forEach { uri ->
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val persistedUris = contentResolver.persistedUriPermissions.map { it.uri }

                if (!persistedUris.contains(uri)) {  // ✅ Avoid redundant permission requests
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                }

                val bitmap = loadImage(uri)
                processImage(uri, bitmap)
            } catch (e: SecurityException) {
                Log.e("PermissionError", "Failed to persist URI permission: ${e.message}")
            }
        }
    }

    private fun loadImage(uri: Uri): Bitmap {
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalArgumentException("Invalid URI or image loading failed")
    }

    private fun processImage(uri: Uri, bitmap: Bitmap) {
        try {
            val classificationResult = if (currentModel == "mobilenet_v1") {
                processImageModel1(bitmap)
            } else {
                processImageModel2(bitmap)
            }

            classificationResult?.let { (label, confidence) ->
                val result = ImageResult(
                    imagePath = uri.toString(),
                    modelName = currentModel,
                    label = label,
                    confidence = confidence
                )
                imageViewModel.saveResult(result)
            } ?: Log.d("ImageProcessing", "Classification failed")

        } catch (e: Exception) {
            Log.e("ImageProcessing", "Error processing image: ${e.message}")
        }
    }

    private fun processImageModel1(bitmap: Bitmap): Pair<String, Float>? {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        return imageClassifier.classify(resizedBitmap)
    }

    private fun processImageModel2(bitmap: Bitmap): Pair<String, Float>? {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        return imageClassifier.classifyModel2(resizedBitmap)
    }
    private fun applyFilters() {
        val minConfidence = editTextMinConfidence.text.toString().toFloatOrNull() ?: 0f
        val maxConfidence = editTextMaxConfidence.text.toString().toFloatOrNull() ?: 1f
        val topN = editTextTopN.text.toString().toIntOrNull() ?: Int.MAX_VALUE
        val label = editTextSearch.text.toString()
        val modelFilter = spinnerFilter.selectedItem.toString()
        val sortOption = when (spinnerSortOptions.selectedItem.toString()) {
            "Confidence (High to Low)" -> "desc"
            "Confidence (Low to High)" -> "asc"
            else -> "desc"
        }

        imageViewModel.applyFilters(minConfidence, maxConfidence, topN, label, modelFilter, sortOption)
    }

    private fun clearFilters() {
        editTextSearch.text.clear()
        editTextMinConfidence.text.clear()
        editTextMaxConfidence.text.clear()
        editTextTopN.text.clear()
        spinnerSortOptions.setSelection(0)

        imageViewModel.loadAllResults()
    }

}
