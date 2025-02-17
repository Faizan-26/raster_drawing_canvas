import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:raster_drawing_canvas/core/types.dart';

/// An implementation of [RasterDrawingCanvasPlatform] that uses method channels.
class MethodChannelRasterDrawingCanvas {
  @visibleForTesting
  final methodChannel = const MethodChannel('raster_drawing_canvas');

  Future<void> undo() async {
    await methodChannel.invokeMethod<void>('undo');
  }

  Future<void> redo() async {
    await methodChannel.invokeMethod<void>('redo');
  }

  Future<void> clear() async {
    await methodChannel.invokeMethod<void>('clear');
  }

  Future<void> setBrushThickness(double thickness) async {
    await methodChannel.invokeMethod<void>('setBrushThickness', {
      'thickness': thickness,
    });
  }

  Future<void> setBrushColor(int color) async {
    await methodChannel.invokeMethod<void>('setBrushColor', {'color': color});
  }

  Future<void> setBrushProperties(Map<String, dynamic> properties) async {
    await methodChannel.invokeMethod<void>('setBrushProperties', {
      'properties': properties,
    });
  }

  Future<void> setBrushType(String type) async {
    await methodChannel.invokeMethod<void>('setBrushType', {'type': type});
  }

  // reset canvas transformation
  Future<void> resetCanvas() async {
    await methodChannel.invokeMethod<void>('resetCanvasTransformation');
  }

  Future<String?> saveImage(
    ImageQuality quality,
    String filePath,
    ImageFormat format,
  ) async {
    return await methodChannel.invokeMethod<String>('saveAsImage', {
      'filePath': filePath,
      'format': format.name,
      'quality': quality.value,
    });
  }

  Future<void> setCanvasColor(int color) async {
    await methodChannel.invokeMethod<void>('setCanvasColor', {'color': color});
  }

  void dispose() {
    methodChannel.setMethodCallHandler(null);
  }
}
