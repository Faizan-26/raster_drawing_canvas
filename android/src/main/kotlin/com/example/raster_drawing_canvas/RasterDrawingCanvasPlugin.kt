package com.example.raster_drawing_canvas

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Paint
import android.graphics.Bitmap

class RasterDrawingCanvasPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "raster_drawing_canvas")
        channel.setMethodCallHandler(this)
        
        // Register the platform view factory
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            "raster_drawing_canvas_view",
            DrawingViewFactory(flutterPluginBinding.binaryMessenger)
        )
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val drawingView = DrawingViewManager.drawingView
        
        if (drawingView == null) {
            result.error("NO_VIEW", "No drawing view found", null)
            return
        }

        when (call.method) {
            "undo" -> {
                drawingView.undo()
                result.success(null)
            }
            "redo" -> {
                drawingView.redo()
                result.success(null)
            }
            "clear" -> {
                drawingView.clearCanvas()
                result.success(null)
            }
            "setRandomColor" -> {
                drawingView.setRandomColor()
                result.success(null)
            }
            "setBrushThickness" -> {
                val thickness = call.argument<Double>("thickness")?.toFloat()
                if (thickness != null) {
                    drawingView.setBrushThickness(thickness)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENT", "Thickness must be provided", null)
                }
            }
            "setBrushColor" -> {
                val color = call.argument<Number>("color")?.toInt()
                if (color != null) {
                    drawingView.setBrushColor(color)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENT", "Color must be provided", null)
                }
            }
            "setBrushProperties" -> {
                @Suppress("UNCHECKED_CAST")
                val properties = call.argument<Map<String, Any>>("properties")
                if (properties != null) {
                    drawingView.updateBrushProperties(properties)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENT", "Properties must be provided", null)
                }
            }
            "setBrushType" -> {
                val type = call.argument<String>("type")
                if (type != null) {
                    drawingView.brushType = BrushType.valueOf(type.uppercase())
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENT", "Brush type must be provided", null)
                }
            }
            "resetCanvasTransformation" -> {
                drawingView.resetTransform()
                result.success(null)
            }
            "setCanvasColor" -> {
                val color = call.argument<Number>("color")?.toInt()
                if (color != null) {
                    drawingView.setCanvasColor(color)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENT", "Color must be provided", null)
                }
            }
            "saveAsImage" -> {
                val filePath = call.argument<String>("filePath")
                val format = call.argument<String>("format")
                val quality = call.argument<Int>("quality") ?: 100

                if (filePath != null && format != null) {
                    val success = drawingView.saveAsImage(filePath, format, quality)
                    if (success) {
                        result.success(filePath)
                    } else {
                        result.error("SAVE_FAILED", "Failed to save image", null)
                    }
                } else {
                    result.error("INVALID_ARGUMENT", "File path and format must be provided", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
