package com.ridexpress.driver_app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _available = MutableStateFlow(true)
    val available = _available.asStateFlow()

    fun setAvailable(b: Boolean) { _available.value = b }
}
