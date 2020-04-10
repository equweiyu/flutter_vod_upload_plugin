package baitianwei.flutter_vod_upload_plugin

import android.content.Context

import com.alibaba.sdk.android.vod.upload.VODUploadCallback
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo
import com.alibaba.sdk.android.vod.upload.model.VodInfo
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterVodUploadPlugin */
public class FlutterVodUploadPlugin(private val context: Context): FlutterPlugin, MethodCallHandler {
  val uploader: VODUploadClientImpl = VODUploadClientImpl(context)

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_vod_upload_plugin")
    channel.setMethodCallHandler(FlutterVodUploadPlugin(flutterPluginBinding.applicationContext));
  }


  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_vod_upload_plugin")
      channel.setMethodCallHandler(FlutterVodUploadPlugin(registrar.activeContext().applicationContext))
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }
  fun setUploadAuthAndAddress(uploadAuth: String, uploadAddress: String) {

    val callback: VODUploadCallback = object : VODUploadCallback() {
      override fun onUploadStarted(uploadFileInfo: UploadFileInfo?) {
        uploader.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress)
      }
    }
    uploader.init(callback);
  }
  fun addFile(filePath:String) {
    val vodInfo:VodInfo = VodInfo()
    vodInfo.title = "title"

    uploader.addFile(filePath, vodInfo)
  }

  fun start() {
    uploader.start()
  }
  fun stop() {
    uploader.stop()
  }
  fun pause() {
    uploader.pause()
  }
  fun resume() {
    uploader.resume()
  }
}