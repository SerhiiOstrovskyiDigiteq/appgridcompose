package com.digiteq.appgridcompose

import androidx.compose.ui.graphics.Color

data class AppShortcut (
    val id: Int,
    val title: String = id.toString(),
    val color: Color,
    var isDragged: Boolean = false
)
