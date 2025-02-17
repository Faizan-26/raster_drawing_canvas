import 'package:flutter/material.dart';
import 'package:raster_drawing_canvas/drawing_view.dart';
import 'package:raster_drawing_canvas/core/types.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(useMaterial3: true),
      home: const DrawingScreen(),
    );
  }
}

class DrawingScreen extends StatefulWidget {
  const DrawingScreen({super.key});

  @override
  State<DrawingScreen> createState() => _DrawingScreenState();
}

class _DrawingScreenState extends State<DrawingScreen> {
  final RasterDrawingController _controller = RasterDrawingController();
  double _thickness = 5.0;
  Color _selectedColor = Colors.black;
  Color _canvasColor = Colors.white;
  String _currentTool = 'pen';

  Future<void> _saveImage() async {
    try {
      final directory = await getDownloadsDirectory();
      final String fileName =
          'drawing_${DateTime.now().millisecondsSinceEpoch}';

      final ImageFormat? format = await showDialog<ImageFormat>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Select Format'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: ImageFormat.values.map((format) {
              return ListTile(
                title: Text(format.name.toUpperCase()),
                onTap: () => Navigator.pop(context, format),
              );
            }).toList(),
          ),
        ),
      );

      if (format == null || directory == null) {
        throw 'Invalid format or Directory';
      }

      final String filePath = '${directory.path}/$fileName.${format.name}';
      final result = await _controller.saveImage(
        filePath: filePath,
        format: format,
        quality: ImageQuality.high,
      );

      if (result != null) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Image saved to downloads folder'),
              duration: const Duration(seconds: 2),
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to save image')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Drawing App"),
        actions: [
          IconButton(icon: const Icon(Icons.undo), onPressed: _controller.undo),
          IconButton(icon: const Icon(Icons.redo), onPressed: _controller.redo),
          IconButton(
            icon: const Icon(Icons.delete),
            onPressed: _controller.clearCanvas,
          ),
          PopupMenuButton<void>(
            icon: const Icon(Icons.more_vert),
            itemBuilder: (context) => [
              PopupMenuItem(
                child: const ListTile(
                  leading: Icon(Icons.refresh),
                  title: Text('Reset Canvas'),
                ),
                onTap: _controller.resetCanvasTransformation,
              ),
              PopupMenuItem(
                child: const ListTile(
                  leading: Icon(Icons.save),
                  title: Text('Save Image'),
                ),
                onTap: () => Future.delayed(
                  const Duration(milliseconds: 50),
                  _saveImage,
                ),
              ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(child: RasterDrawingView(controller: _controller)),
          Container(
            padding: const EdgeInsets.all(8),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _buildToolButton('pen', Icons.edit),
                    _buildToolButton('marker', Icons.brush),
                    _buildToolButton('highlighter', Icons.highlight),
                    _buildToolButton('eraser', Icons.auto_fix_high),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    const SizedBox(width: 16),
                    const Icon(Icons.line_weight),
                    Expanded(
                      child: Slider(
                        value: _thickness,
                        min: 1,
                        max: 250,
                        onChanged: (value) {
                          setState(() => _thickness = value);
                          _updateCurrentTool();
                        },
                      ),
                    ),
                  ],
                ),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Row(
                    children: [
                      if (_currentTool != 'eraser') ...[
                        const Text('Brush: '),
                        const SizedBox(width: 8),
                        for (final color in [
                          Colors.black,
                          Colors.red,
                          Colors.blue,
                          Colors.green,
                          Colors.yellow,
                          Colors.purple,
                          Colors.orange,
                          Colors.pink,
                        ])
                          _buildColorButton(color, false),
                      ],
                      const SizedBox(width: 16),
                      const Text('Canvas: '),
                      const SizedBox(width: 8),
                      for (final color in [
                        Colors.white,
                        Colors.black,
                        Colors.grey,
                        Colors.yellowAccent,
                        Colors.blueAccent,
                      ])
                        _buildColorButton(color, true),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildToolButton(String tool, IconData icon) {
    return IconButton(
      icon: Icon(icon),
      color: _currentTool == tool ? Theme.of(context).primaryColor : null,
      onPressed: () {
        setState(() => _currentTool = tool);
        _updateCurrentTool();
      },
    );
  }

  Widget _buildColorButton(Color color, bool isCanvasColor) {
    final isSelected =
        isCanvasColor ? _canvasColor == color : _selectedColor == color;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: InkWell(
        onTap: () {
          setState(() {
            if (isCanvasColor) {
              _canvasColor = color;
              _controller.setCanvasColor(color);
            } else {
              _selectedColor = color;
              _updateCurrentTool();
            }
          });
        },
        child: Container(
          width: 32,
          height: 32,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(
              color: isSelected ? Theme.of(context).primaryColor : Colors.grey,
              width: isSelected ? 2 : 1,
            ),
          ),
        ),
      ),
    );
  }

  void _updateCurrentTool() {
    switch (_currentTool) {
      case 'pen':
        _controller.setPen(color: _selectedColor, thickness: _thickness);
        break;
      case 'marker':
        _controller.setMarker(color: _selectedColor, thickness: _thickness);
        break;
      case 'highlighter':
        _controller.setHighlighter(
          color: _selectedColor,
          thickness: _thickness,
        );
        break;
      case 'eraser':
        _controller.setEraser(thickness: _thickness);
        break;
    }
  }
}
