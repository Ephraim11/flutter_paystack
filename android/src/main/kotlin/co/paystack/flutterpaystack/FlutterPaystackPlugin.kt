package co.paystack.flutterpaystack

import android.app.Activity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger

class FlutterPaystackPlugin : FlutterPlugin, ActivityAware {

    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var methodCallHandler: MethodCallHandlerImpl? = null
    private var activity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = binding
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = null
        methodCallHandler = null
    }

    private fun setupMethodHandler(messenger: BinaryMessenger, activity: Activity) {
        methodCallHandler = MethodCallHandlerImpl(messenger, activity)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        pluginBinding?.let {
            setupMethodHandler(it.binaryMessenger, activity!!)
        }
    }

    override fun onDetachedFromActivity() {
        methodCallHandler?.disposeHandler()
        methodCallHandler = null
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }
}
