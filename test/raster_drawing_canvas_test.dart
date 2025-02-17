// import 'package:flutter_test/flutter_test.dart';
// import 'package:raster_drawing_canvas/raster_drawing_canvas.dart';
// import 'package:raster_drawing_canvas/raster_drawing_canvas_platform_interface.dart';
// import 'package:raster_drawing_canvas/raster_drawing_canvas_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';

// class MockRasterDrawingCanvasPlatform
//     with MockPlatformInterfaceMixin
//     implements RasterDrawingCanvasPlatform {

//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }

// void main() {
//   final RasterDrawingCanvasPlatform initialPlatform = RasterDrawingCanvasPlatform.instance;

//   test('$MethodChannelRasterDrawingCanvas is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelRasterDrawingCanvas>());
//   });

//   test('getPlatformVersion', () async {
//     RasterDrawingCanvas rasterDrawingCanvasPlugin = RasterDrawingCanvas();
//     MockRasterDrawingCanvasPlatform fakePlatform = MockRasterDrawingCanvasPlatform();
//     RasterDrawingCanvasPlatform.instance = fakePlatform;

//     expect(await rasterDrawingCanvasPlugin.getPlatformVersion(), '42');
//   });
// }
