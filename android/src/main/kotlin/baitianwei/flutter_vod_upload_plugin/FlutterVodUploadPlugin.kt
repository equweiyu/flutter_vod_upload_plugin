package baitianwei.flutter_vod_upload_plugin

import android.app.Activity
import android.app.Application
import androidx.annotation.NonNull
import com.alibaba.sdk.android.vod.upload.VODUploadCallback
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo
import com.alibaba.sdk.android.vod.upload.model.VodInfo
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


/** FlutterVodUploadPlugin */
public class FlutterVodUploadPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    var uploader: VODUploadClientImpl? = null


    private var channel: MethodChannel? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var application: Application? = null
    private var activity: Activity? = null

    fun setup(messenger: BinaryMessenger, application: Application, activity: Activity) {
        this.activity = activity
        this.application = application
        channel = MethodChannel(messenger, "flutter_vod_upload_plugin")
        channel!!.setMethodCallHandler(this)
        uploader = VODUploadClientImpl(application)
    }

    private fun tearDown() {
        activityBinding = null
        channel?.setMethodCallHandler(null)
        channel = null
        application = null
    }


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = flutterPluginBinding
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activityBinding = binding
        setup(
                pluginBinding!!.binaryMessenger,
                (pluginBinding!!.applicationContext as Application),
                activityBinding!!.activity)

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }


    override fun onDetachedFromActivity() {
        tearDown()
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }


    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            if (registrar.activity() == null) return
            val activity = registrar.activity()
            var application: Application? = null
            if (registrar.context() != null) {
                application = registrar.context().applicationContext as Application
            }
            val plugin = FlutterVodUploadPlugin()
            plugin.setup(registrar.messenger(), application!!, activity)
        }
    }

    private fun invokeMethod(method: String, arguments: Any?) {
        activity?.runOnUiThread {
            channel?.invokeMethod(method, arguments)
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
                val vodInfo = VodInfo()
                val info = call.argument<Map<*, *>>("vodInfo")
                vodInfo.title = info?.get("title") as? String
                if (info?.get("isShowWaterMark") is Boolean) {
                    vodInfo.isShowWaterMark = info["isShowWaterMark"] as? Boolean
                }
                addFile(call.argument<String>("filePath")!!, vodInfo)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun setUploadAuthAndAddress(uploadAuth: String, uploadAddress: String) {

        val callback: VODUploadCallback = object : VODUploadCallback() {
            override fun onUploadSucceed(info: UploadFileInfo?) {
                invokeMethod("onUploadSucceed", null)
            }

            override fun onUploadFailed(info: UploadFileInfo?, code: String?, message: String?) {
                invokeMethod("onUploadFailed", mapOf("code" to code, "message" to message))
            }

            override fun onUploadProgress(info: UploadFileInfo?, uploadedSize: Long, totalSize: Long) {
                invokeMethod("onUploadProgress", mapOf("uploadedSize" to uploadedSize, "totalSize" to totalSize))
            }

            override fun onUploadTokenExpired() {
                invokeMethod("onUploadTokenExpired", null)
            }

            override fun onUploadRetry(code: String?, message: String?) {
                invokeMethod("onUploadRetry", null)
            }

            override fun onUploadRetryResume() {
                invokeMethod("onUploadRetryResume", null)
            }

            override fun onUploadStarted(uploadFileInfo: UploadFileInfo?) {
                invokeMethod("onUploadStarted", null)
                uploader?.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress)
            }
        }
        uploader?.init(callback)
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
