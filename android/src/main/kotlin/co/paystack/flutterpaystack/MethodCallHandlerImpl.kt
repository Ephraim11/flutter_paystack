package co.paystack.flutterpaystack

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

class MethodCallHandlerImpl(messenger: BinaryMessenger, private val activity: Activity?) : MethodCallHandler {
    private var channel: MethodChannel? = null
    private var authDelegate: AuthDelegate? = null

    init {
        activity?.let {
            authDelegate = AuthDelegate(it)
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
                authDelegate?.handleAuthorization(result, call)
            }
            "getEncryptedData" -> {
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

    fun disposeHandler() {
        channel?.setMethodCallHandler(null)
        channel = null
    }
}

private const val channelName = "plugins.wilburt/flutter_paystack"
