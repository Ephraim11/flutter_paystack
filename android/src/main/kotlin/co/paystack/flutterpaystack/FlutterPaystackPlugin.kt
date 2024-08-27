package co.paystack.flutterpaystack

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class FlutterPaystackPlugin : FlutterPlugin, ActivityAware {

    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var methodCallHandler: MethodCallHandlerImpl? = null

    // When the plugin is attached to the Flutter engine
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = binding
        // Setup method handler only if the activity is already available
        methodCallHandler?.let { handler ->
            pluginBinding?.binaryMessenger?.let { messenger ->
                handler.setMessenger(messenger)
            }
        }
    }

    // When the plugin is detached from the Flutter engine
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodCallHandler?.disposeHandler()
        methodCallHandler = null
        pluginBinding = null
    }

    // Setup method handler with messenger and activity
    private fun setupMethodHandler(messenger: BinaryMessenger, activity: Activity) {
        methodCallHandler = MethodCallHandlerImpl(messenger, activity)
    }

    // When the plugin is attached to an activity
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        pluginBinding?.let { binding ->
            setupMethodHandler(binding.binaryMessenger, binding.activity)
        }
    }

    // When the plugin is detached from an activity
    override fun onDetachedFromActivity() {
        methodCallHandler?.disposeHandler()
        methodCallHandler = null
    }

    // When the plugin is detached from an activity due to a configuration change (e.g. screen rotation)
    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    // When the plugin is reattached to an activity after a configuration change (e.g. screen rotation)
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }
}
