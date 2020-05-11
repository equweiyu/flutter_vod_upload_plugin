package baitianwei.flutter_vod_upload_plugin

import androidx.annotation.NonNull
import com.alibaba.sdk.android.vod.upload.VODUploadCallback
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo
import com.alibaba.sdk.android.vod.upload.model.VodInfo
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterVodUploadPlugin */
public class FlutterVodUploadPlugin : FlutterPlugin, MethodCallHandler {
    var uploader: VODUploadClientImpl? = null
    var methodChannel: MethodChannel? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_vod_upload_plugin")
        val plugin = FlutterVodUploadPlugin()
        plugin.uploader = VODUploadClientImpl(flutterPluginBinding.applicationContext)
        channel.setMethodCallHandler(plugin)
        plugin.methodChannel = channel
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_vod_upload_plugin")
            val plugin = FlutterVodUploadPlugin()
            plugin.uploader = VODUploadClientImpl(registrar.context())
            channel.setMethodCallHandler(plugin)
            plugin.methodChannel = channel
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "start" -> start()
            "pause" -> pause()
            "stop" -> stop()
            "resume" -> resume()
            "setListener" -> {
                setUploadAuthAndAddress(call.argument<String>("uploadAuth")!!, call.argument<String>("uploadAddress")!!)
            }
            "addFile" -> {
                var vodInfo = VodInfo()
                val info = call.argument<Map<*, *>>("vodInfo")
                vodInfo.title = info?.get("title") as? String
                if (info?.get("isShowWaterMark") is Boolean) {
                    vodInfo.isShowWaterMark = info?.get("isShowWaterMark") as? Boolean
                }
                addFile(call.argument<String>("filePath")!!, vodInfo)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    private fun setUploadAuthAndAddress(uploadAuth: String, uploadAddress: String) {

        val callback: VODUploadCallback = object : VODUploadCallback() {
            override fun onUploadSucceed(info: UploadFileInfo?) {
                methodChannel?.invokeMethod("onUploadSucceed", null)
            }

            override fun onUploadFailed(info: UploadFileInfo?, code: String?, message: String?) {
                methodChannel?.invokeMethod("onUploadFailed", mapOf("code" to code, "message" to message))
            }

            override fun onUploadProgress(info: UploadFileInfo?, uploadedSize: Long, totalSize: Long) {
                methodChannel?.invokeMethod("onUploadProgress", mapOf("uploadedSize" to uploadedSize, "totalSize" to totalSize))
            }

            override fun onUploadTokenExpired() {
                methodChannel?.invokeMethod("onUploadTokenExpired", null)
            }

            override fun onUploadRetry(code: String?, message: String?) {
                methodChannel?.invokeMethod("onUploadRetry", null)
            }

            override fun onUploadRetryResume() {
                methodChannel?.invokeMethod("onUploadRetryResume", null)
            }

            override fun onUploadStarted(uploadFileInfo: UploadFileInfo?) {
                methodChannel?.invokeMethod("onUploadStarted", null)
                uploader?.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress)
            }
        }
        uploader?.init(callback);
    }

    private fun addFile(filePath: String, vodInfo: VodInfo) {
        uploader?.addFile(filePath, vodInfo)
    }

    private fun start() {
        uploader?.start()
    }

    private fun stop() {
        uploader?.stop()
    }

    private fun pause() {
        uploader?.pause()
    }

    private fun resume() {
        uploader?.resume()
    }
}
