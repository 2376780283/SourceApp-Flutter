import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Source Engine App',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.orange),
        useMaterial3: true,
      ),
      home: const IndexPage(title: 'Source Engine App'),
    );
  }
}

class IndexPage extends StatefulWidget {
  const IndexPage({super.key, required this.title});

  final String title;

  @override
  State<IndexPage> createState() => _IndexPageState();
}

class _IndexPageState extends State<IndexPage> {
  final TextEditingController _pathController = TextEditingController(
      text: '/storage/emulated/0/半条命2/Half-Life 2/Half-Life 2/');
  final TextEditingController _argsController =
      TextEditingController(text: '-nobackgroundlevel -full');
  String _selectedGame = 'hl2';
  bool _isDevEnabled = false;
  bool _isConsoleEnabled = true;
  bool _isGamepadUIEnabled = false;
  bool _isFpsEnabled = false;
  bool _permissionsGranted = false;

  final List<String> _games = ['hl2', 'hl1', 'cstrike', 'portal', 'episodic', 'ep2'];

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    final microphone = await Permission.microphone.status;
    final storage = await Permission.manageExternalStorage.status;
    
    setState(() {
      _permissionsGranted = microphone.isGranted && storage.isGranted;
    });
  }

  Future<void> _requestPermissions() async {
    final statuses = await [
      Permission.microphone,
      Permission.manageExternalStorage,
    ].request();

    setState(() {
      _permissionsGranted = statuses[Permission.microphone]!.isGranted &&
          statuses[Permission.manageExternalStorage]!.isGranted;
    });

    if (_permissionsGranted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Permissions granted!')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Permissions denied!')),
      );
    }
  }

  Future<void> _launchGame() async {
    const platform = MethodChannel('runIntermediaryActivity');
    try {
      final Map<String, dynamic> settings = {
        'game_path': _pathController.text,
        'game_argv': _argsController.text,
        'selected_game': _selectedGame,
        'enable_dev': _isDevEnabled,
        'console_switch': _isConsoleEnabled,
        'gamepad_ui': _isGamepadUIEnabled,
        'enable_fps': _isFpsEnabled,
      };
      await platform.invokeMethod('runGame', settings);
    } on PlatformException catch (e) {
      debugPrint("Failed to launch game: '${e.message}'.");
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Game error: ${e.message}')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              controller: _pathController,
              decoration: const InputDecoration(
                labelText: 'Game Path',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _argsController,
              decoration: const InputDecoration(
                labelText: 'Command Line Arguments',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _selectedGame,
              decoration: const InputDecoration(
                labelText: 'Selected Game',
                border: OutlineInputBorder(),
              ),
              items: _games.map((String game) {
                return DropdownMenuItem<String>(
                  value: game,
                  child: Text(game),
                );
              }).toList(),
              onChanged: (String? newValue) {
                setState(() {
                  _selectedGame = newValue!;
                });
              },
            ),
            const SizedBox(height: 16),
            SwitchListTile(
              title: const Text('Developer Mode'),
              value: _isDevEnabled,
              onChanged: (bool value) {
                setState(() {
                  _isDevEnabled = value;
                });
              },
            ),
            SwitchListTile(
              title: const Text('Enable Console'),
              value: _isConsoleEnabled,
              onChanged: (bool value) {
                setState(() {
                  _isConsoleEnabled = value;
                });
              },
            ),
            SwitchListTile(
              title: const Text('Gamepad UI'),
              value: _isGamepadUIEnabled,
              onChanged: (bool value) {
                setState(() {
                  _isGamepadUIEnabled = value;
                });
              },
            ),
            SwitchListTile(
              title: const Text('Show FPS'),
              value: _isFpsEnabled,
              onChanged: (bool value) {
                setState(() {
                  _isFpsEnabled = value;
                });
              },
            ),
            const SizedBox(height: 32),
            Center(
              child: ElevatedButton.icon(
                onPressed: _requestPermissions,
                icon: const Icon(Icons.security),
                label: const Text('REQUEST PERMISSIONS'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 15),
                  textStyle: const TextStyle(fontSize: 16),
                ),
              ),
            ),
            const SizedBox(height: 12),
            if (_permissionsGranted)
              Center(
                child: Text(
                  '✓ Permissions Granted',
                  style: TextStyle(color: Colors.green, fontSize: 16, fontWeight: FontWeight.bold),
                ),
              )
            else
              Center(
                child: Text(
                  '✗ Permissions Not Granted',
                  style: TextStyle(color: Colors.red, fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),
            const SizedBox(height: 32),
            Center(
              child: ElevatedButton.icon(
                onPressed: _permissionsGranted ? _launchGame : null,
                icon: const Icon(Icons.play_arrow),
                label: const Text('START GAME'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 15),
                  textStyle: const TextStyle(fontSize: 20),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
