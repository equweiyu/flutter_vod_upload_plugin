import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:flutter_vod_upload_plugin/flutter_vod_upload_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  TextEditingController _uploadAddressEditingController =
      TextEditingController();
  TextEditingController _uploadAuthEditingController = TextEditingController();
  File _file;
  num _progressValue;
  @override
  void initState() {
    super.initState();
  }

  Future getVideoFile() async {
    File file = await ImagePicker.pickVideo(source: ImageSource.gallery);

    setState(() {
      _file = file;
    });
  }

  Future upload() async {
    if (_file == null) return;
    final path = _file.path;

    final String uploadAuth = _uploadAuthEditingController.text;
    final String uploadAddress = _uploadAddressEditingController.text;
    if (uploadAuth.isEmpty || uploadAddress.isEmpty) return;

    FlutterVodUploadPlugin.setListener(uploadAuth, uploadAddress);
    FlutterVodUploadPlugin.callBack(
      onUploadStarted: () async {
        print("onUploadStarted");
      },
      onUploadProgress: (uploadedSize, totalSize) {
        setState(() {
          _progressValue = uploadedSize / totalSize;
        });
      },
    );
    FlutterVodUploadPlugin.stop();
    FlutterVodUploadPlugin.addFile(
      path,
      VodInfo(
        title: path,
      ),
    );
    FlutterVodUploadPlugin.start();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ListView(
          children: <Widget>[
            Offstage(
              offstage: _progressValue == null,
              child: LinearProgressIndicator(
                value: _progressValue?.toDouble(),
              ),
            ),
            ListTile(
              title: TextField(
                decoration: InputDecoration(hintText: 'uploadAddress'),
                controller: _uploadAddressEditingController,
              ),
            ),
            ListTile(
              title: TextField(
                decoration: InputDecoration(hintText: 'uploadAuth'),
                controller: _uploadAuthEditingController,
              ),
            ),
            ListTile(
              title: Text('${_file?.path}'),
              trailing: IconButton(
                  icon: Icon(Icons.video_library),
                  onPressed: () => getVideoFile()),
            ),
            ListTile(
              trailing: IconButton(
                icon: Icon(Icons.file_upload),
                onPressed: () => upload(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
