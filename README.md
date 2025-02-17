# Raster Drawing Canvas

A Flutter plugin that provides a powerful and efficient canvas for drawing and sketching functionality in Flutter applications. This plugin allows users to create freehand drawings, sketches, and annotations with various customization options.

## Features

- Smooth freehand drawing experience
- Customizable brush styles and sizes
- Support for different colors and opacity
- Ability to save drawings as images
- Undo/Redo functionality
- Platform-specific optimizations for Android

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  raster_drawing_canvas: ^latest_version
```

## Usage

Here's a simple example of how to use the RasterDrawingCanvas in your Flutter app:

```dart
import 'package:raster_drawing_canvas/drawing_view.dart';

class DrawingScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Drawing Canvas')),
      body: DrawingView(
        // Configure canvas properties here
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height,
        // Add your drawing callbacks here
        onDrawingComplete: (bytes) {
          // Handle the drawing data
        },
      ),
    );
  }
}
```

### Customization Options

You can customize the drawing experience with various properties:

```dart
DrawingView(
  strokeWidth: 5.0,
  strokeColor: Colors.blue,
  backgroundColor: Colors.white,
  enableEraser: true,
  // Other properties...
)
```

## Platform Support

Currently, this plugin supports:
- Android

To add support for additional platforms, run:
```bash
flutter create -t plugin --platforms <platforms> .
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Additional Resources

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
