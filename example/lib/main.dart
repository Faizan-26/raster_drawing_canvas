import 'package:flutter/material.dart';
import 'package:raster_drawing_canvas/drawing_view.dart';

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
  String _currentTool = 'pen';

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
          // Added button to reset canvas transformation
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _resetTransformations,
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
                if (_currentTool != 'eraser')
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      children: [
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
                          _buildColorButton(color),
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

  Widget _buildColorButton(Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: InkWell(
        onTap: () {
          setState(() => _selectedColor = color);
          _updateCurrentTool();
        },
        child: Container(
          width: 32,
          height: 32,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(
              color:
                  _selectedColor == color
                      ? Theme.of(context).primaryColor
                      : Colors.grey,
              width: _selectedColor == color ? 2 : 1,
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

  void _resetTransformations() {
    _controller.resetCanvasTransformation();
  }
}
