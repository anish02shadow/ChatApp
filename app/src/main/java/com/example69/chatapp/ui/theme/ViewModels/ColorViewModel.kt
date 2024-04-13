package com.example69.chatapp.ui.theme.ViewModels


import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

class ColorViewModel : ViewModel() {
    private val _colors = mutableMapOf<String, androidx.compose.ui.graphics.Color>()
    val colors: Map<String, androidx.compose.ui.graphics.Color> = _colors

    fun getColor(key: String): androidx.compose.ui.graphics.Color {
        return _colors.getOrPut(key) { pickRandomColor() }
    }

    public fun pickRandomColor() = Color(
        arrayListOf(
            0xFFE57373, 0xFFBA68C8, 0xFF9575CD, 0xFFF06292,
            0xFF64B5F6, 0xFF4DD0E1, 0xFFFF8A65,
            0xFFFFD54F, 0xFF81C784, 0xFFFFF176,
        ).random()
    )
}