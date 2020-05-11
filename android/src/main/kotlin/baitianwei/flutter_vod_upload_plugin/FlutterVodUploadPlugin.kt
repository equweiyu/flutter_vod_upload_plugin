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
public class FlutterVodUploadPlugin : FlutterPlugin, MethodCallHandler {
    var uploader: VODUploadClientImpl? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_vod_upload_plugin")
        val plugin = FlutterVodUploadPlugin()
        plugin.uploader = VODUploadClientImpl(flutterPluginBinding.applicationContext)
        channel.setMethodCallHandler(plugin);
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
            val plugin = FlutterVodUploadPlugin()
            plugin.uploader = VODUploadClientImpl(registrar.activeContext())
            channel.setMethodCallHandler(plugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "start") {
            start()
        } else if (call.method == "pause") {
            pause()
        } else if (call.method == "stop") {
            stop()
        } else if (call.method == "resume") {
            resume()
        } else if (call.method == "setListener") {
            setUploadAuthAndAddress(call.argument("uploadAuth")!!, call.argument("uploadAddress")!!)
        } else if (call.method == "addFile") {
            val filePath: String = call.argument("filePath")!!
            addFile(filePath)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    fun setUploadAuthAndAddress(uploadAuth: String, uploadAddress: String) {

        val callback: VODUploadCallback = object : VODUploadCallback() {
            override fun onUploadStarted(uploadFileInfo: UploadFileInfo?) {
                uploader?.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress)
            }
        }
        uploader?.init(callback);
    }

    fun addFile(filePath: String) {
        val vodInfo: VodInfo = VodInfo()
        vodInfo.title = filePath
        uploader?.addFile(filePath, vodInfo)
    }

    fun start() {
        uploader?.start()
    }

    fun stop() {
        uploader?.stop()
    }

    fun pause() {
        uploader?.pause()
    }

    fun resume() {
        uploader?.resume()
    }
}
