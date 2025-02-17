import 'package:flutter/material.dart';
import 'package:raster_drawing_canvas/core/types.dart';
import 'raster_drawing_canvas_method_channel.dart';

class RasterDrawingController {
  final MethodChannelRasterDrawingCanvas _platform =
      MethodChannelRasterDrawingCanvas();

  Future<void> undo() => _platform.undo();
  Future<void> redo() => _platform.redo();
  Future<void> clearCanvas() => _platform.clear();

  Future<void> setPen({required Color color, required double thickness}) {
    return _platform.setBrushProperties({
      'color': color.value,
      'strokeWidth': thickness,
      'alpha': 255,
      'blendMode': 'SRC_OVER',
      'style': 'STROKE',
      'strokeCap': 'ROUND',
      'strokeJoin': 'ROUND',
      'isEraserMode': false,
    });
  }

  Future<void> setMarker({required Color color, required double thickness}) {
    return _platform.setBrushProperties({
      'color': color.value,
      'strokeWidth': thickness * 2,
      'alpha': 180,
      'blendMode': 'SRC_OVER',
      'style': 'STROKE',
      'strokeCap': 'SQUARE',
      'strokeJoin': 'ROUND',
      'isEraserMode': false,
    });
  }

  Future<void> setHighlighter({
    required Color color,
    required double thickness,
  }) {
    return _platform.setBrushProperties({
      'color': color.withOpacity(0.5).value,
      'strokeWidth': thickness * 3,
      'alpha': 128,
      'blendMode': 'SRC_OVER',
      'style': 'STROKE',
      'strokeCap': 'ROUND',
      'strokeJoin': 'ROUND',
      'isEraserMode': false,
    });
  }

  Future<void> setEraser({
    required double thickness,
    Color color = Colors.white,
  }) {
    return _platform.setBrushProperties({
      'color': color.value,
      'strokeWidth': thickness * 2,
      'alpha': 255,
      'blendMode': 'CLEAR',
      'style': 'STROKE',
      'strokeCap': 'ROUND',
      'strokeJoin': 'ROUND',
      'isEraserMode': true,
    });
  }

  Future<void> setCanvasColor(Color color) async {
    await _platform.setCanvasColor(color.value);
  }

  Future<void> resetCanvasTransformation() async =>
      await _platform.resetCanvas();

  Future<String?> saveImage({
    required String filePath,
    required ImageFormat format,
    ImageQuality quality = ImageQuality.high,
  }) async {
    return await _platform.saveImage(quality, filePath, format);
  }
}

class RasterDrawingView extends StatefulWidget {
  final RasterDrawingController controller;

  const RasterDrawingView({super.key, required this.controller});

  @override
  State<RasterDrawingView> createState() => _RasterDrawingViewState();
}

class _RasterDrawingViewState extends State<RasterDrawingView> {
  @override
  Widget build(BuildContext context) {
    const String viewType = 'raster_drawing_canvas_view';

    return AndroidView(viewType: viewType, onPlatformViewCreated: (_) {});
  }
}
