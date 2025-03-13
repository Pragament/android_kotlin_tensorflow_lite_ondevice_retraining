package com.pragament.mlapp.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.pragament.mlapp.ImageRepository
import com.pragament.mlapp.database.AppDatabase
import com.pragament.mlapp.database.ImageResult
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val imageResultDao = AppDatabase.getInstance(application).imageResultDao()
    private val repository = ImageRepository(application)

    val allResults: LiveData<List<ImageResult>> = imageResultDao.getAllResults()

    private val _filteredResults = MutableLiveData<List<ImageResult>>()
    val filteredResults: LiveData<List<ImageResult>> get() = _filteredResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun saveResult(imageResult: ImageResult) {
        viewModelScope.launch {
            try {
                imageResultDao.insert(imageResult)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to save image result: ${e.message}")
            }
        }
    }

    fun loadAllResults() {
        _filteredResults.postValue(allResults.value ?: emptyList()) // ✅ Reset to all results
    }



    fun applyFilters(
        minConfidence: Float,
        maxConfidence: Float?,
        topN: Int,
        label: String?,
        modelName: String?,
        sortOption: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val filteredList = imageResultDao.getFilteredResults(
                    minConfidence,
                    maxConfidence ?: 1f,
                    label?.takeIf { it.isNotEmpty() },
                    modelName?.takeIf { it.isNotEmpty() } ?: "none",
                    sortOption
                ).take(topN)
                _filteredResults.postValue(filteredList)
            } catch (e: Exception) {
                _errorMessage.postValue("Error applying filters: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
