import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class FlutterVodUploadPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_vod_upload_plugin');

  static void setListener(String uploadAuth, String uploadAddress) {
    _channel.invokeMethod('setListener', {
      'uploadAuth': uploadAuth,
      'uploadAddress': uploadAddress,
    });
  }

  static void addFile(String filePath, VodInfo vodInfo) {
    if (filePath != null) {
      _channel.invokeMethod('addFile', {
        'filePath': filePath,
        'vodInfo': vodInfo.toMap(),
      });
    }
  }

  static void start() {
    _channel.invokeMethod('start');
  }

  static void stop() {
    _channel.invokeMethod('stop');
  }

  static void pause() {
    _channel.invokeMethod('pause');
  }

  static void resume() {
    _channel.invokeMethod('resume');
  }

  static void callBack({
    Function(dynamic info) onUploadSucceed,
    Function(String code, String message, dynamic info) onUploadFailed,
    Function(num uploadedSize, num totalSize, dynamic info) onUploadProgress,
    Function() onUploadTokenExpired,
    Function() onUploadRetry,
    Function() onUploadRetryResume,
    Function(dynamic info) onUploadStarted,
  }) {
    _channel.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onUploadSucceed':
          return onUploadSucceed(call.arguments['info']);
          break;
        case 'onUploadFailed':
          return onUploadFailed(call.arguments['code'],
              call.arguments['message'], call.arguments['info']);
          break;
        case 'onUploadProgress':
          return onUploadProgress(call.arguments['uploadedSize'],
              call.arguments['totalSize'], call.arguments['info']);
          break;
        case 'onUploadTokenExpired':
          return onUploadTokenExpired();
          break;
        case 'onUploadRetry':
          return onUploadRetry();
          break;
        case 'onUploadRetryResume':
          return onUploadRetryResume();
          break;
        case 'onUploadStarted':
          return onUploadStarted(call.arguments['info']);
          break;
        default:
          throw new UnsupportedError("Unrecognized Event");
      }
    });
  }
}
// TODO
class VodUploadAuthAndAddress {
  final String uploadAuth;
  final String uploadAddress;

  VodUploadAuthAndAddress(this.uploadAuth, this.uploadAddress);
  Map<String, String> toMap() =>
      {'uploadAuth': uploadAuth, 'uploadAddress': uploadAddress};
}

class VodInfo {
  String title;
  String desc;
  int cateId;
  List<String> tags;
  String userData;
  String coverUrl;
  bool isProcess = true;
  bool isShowWaterMark;
  int priority;

  VodInfo({
    @required this.title,
    this.desc,
    this.cateId,
    this.tags,
    this.userData,
    this.coverUrl,
    this.isProcess,
    this.isShowWaterMark,
    this.priority,
  });

  Map<String, dynamic> toMap() {
    return {
      'title': this.title,
      'desc': this.desc,
      'cateId': this.cateId,
      'tags': this.tags,
      'userData': this.userData,
      'coverUrl': this.coverUrl,
      'isProcess': this.isProcess,
      'isShowWaterMark': this.isShowWaterMark,
      'priority': this.priority,
    };
  }

  factory VodInfo.fromMap(Map<String, dynamic> map) {
    return new VodInfo(
      title: map['title'] as String,
      desc: map['desc'] as String,
      cateId: map['cateId'] as int,
      tags: map['tags'] as List<String>,
      userData: map['userData'] as String,
      coverUrl: map['coverUrl'] as String,
      isProcess: map['isProcess'] as bool,
      isShowWaterMark: map['isShowWaterMark'] as bool,
      priority: map['priority'] as int,
    );
  }
}
