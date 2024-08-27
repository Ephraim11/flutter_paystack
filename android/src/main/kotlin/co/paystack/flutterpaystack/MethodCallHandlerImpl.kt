package co.paystack.flutterpaystack

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

class MethodCallHandlerImpl(messenger: BinaryMessenger, private var activity: Activity?) : MethodCallHandler {

    private var channel: MethodChannel? = null
    private var authDelegate: AuthDelegate? = null

    init {
        setupChannelAndDelegate(messenger)
    }

    // Initialize the channel and delegate
    private fun setupChannelAndDelegate(messenger: BinaryMessenger) {
        activity?.let { currentActivity ->
            authDelegate = AuthDelegate(currentActivity)
            channel = MethodChannel(messenger, channelName)
            channel?.setMethodCallHandler(this)
        } ?: run {
            // If activity is null, log or handle appropriately
            println("Activity is null in MethodCallHandlerImpl initialization")
        }
    }

    @SuppressLint("HardwareIds")
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getDeviceId" -> {
                // Safely retrieve the device ID
                val deviceId = activity?.contentResolver?.let {
                    Settings.Secure.getString(it, Settings.Secure.ANDROID_ID)
                }
                if (deviceId != null) {
                    result.success("androidsdk_$deviceId")
                } else {
                    result.error("UNAVAILABLE", "Device ID not available", null)
                }
            }
            "getAuthorization" -> {
                // Handle authorization using the delegate
                authDelegate?.handleAuthorization(result, call)
            }
            "getEncryptedData" -> {
                // Encrypt the data and return it
                val stringData = call.argument<String>("stringData")
                if (stringData != null) {
                    val encryptedData = Crypto.encrypt(stringData)
                    result.success(encryptedData)
                } else {
                    result.error("INVALID_ARGUMENT", "stringData is null", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    // Disposes of the method channel when it's no longer needed
    fun disposeHandler() {
        channel?.setMethodCallHandler(null)
        channel = null
    }

    // Update the activity if it changes
    fun updateActivity(newActivity: Activity?) {
        activity = newActivity
        authDelegate = newActivity?.let { AuthDelegate(it) }
    }
}

private const val channelName = "plugins.wilburt/flutter_paystack"
