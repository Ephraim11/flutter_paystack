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
            channel = MethodChannel(messenger, channelName).apply {
                setMethodCallHandler(this@MethodCallHandlerImpl)
            }
        } ?: throw IllegalArgumentException("Activity cannot be null.")
    }

    @SuppressLint("HardwareIds")
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getDeviceId" -> {
                // Fetches the device ID and prefixes with "androidsdk_"
                val deviceId = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                result.success("androidsdk_$deviceId")
            }
            "getAuthorization" -> {
                // Handles card authorization via AuthDelegate
                authDelegate?.handleAuthorization(result, call) ?: result.error(
                    "AUTH_DELEGATE_ERROR",
                    "Authorization delegate is not initialized",
                    null
                )
            }
            "getEncryptedData" -> {
                // Encrypts the provided data using the Crypto class
                try {
                    val dataToEncrypt = call.argument<String>("stringData") ?: ""
                    val encryptedData = Crypto.encrypt(dataToEncrypt)
                    result.success(encryptedData)
                } catch (e: Exception) {
                    result.error("ENCRYPTION_ERROR", "Failed to encrypt data: ${e.message}", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    fun disposeHandler() {
        channel?.setMethodCallHandler(null)
        channel = null
    }
}

private const val channelName = "plugins.wilburt/flutter_paystack"
