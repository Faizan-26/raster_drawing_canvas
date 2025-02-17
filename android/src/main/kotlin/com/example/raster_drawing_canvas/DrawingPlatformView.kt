package com.example.raster_drawing_canvas

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.flutter.plugin.platform.PlatformView
import com.example.raster_drawing_canvas.DrawingView
import com.example.raster_drawing_canvas.DrawingViewManager

class DrawingPlatformView(context: Context, id: Int, creationParams: Any?) : PlatformView {
    private val drawingView: DrawingView = DrawingView(context, null).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    init {
        DrawingViewManager.drawingView = drawingView
    }

    override fun getView(): View = drawingView

    override fun dispose() {
        if (DrawingViewManager.drawingView == drawingView) {
            DrawingViewManager.drawingView = null
        }
    }
}