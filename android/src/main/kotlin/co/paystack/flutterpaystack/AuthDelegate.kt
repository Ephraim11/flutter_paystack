package co.paystack.flutterpaystack

import android.app.Activity
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Wilberforce on 26/07/18 at 18:35.
 */
class AuthDelegate(private val activity: Activity) {

    private var pendingResult: MethodChannel.Result? = null

    fun handleAuthorization(pendingResult: MethodChannel.Result, methodCall: MethodCall) {
        if (!setPendingResult(pendingResult)) {
            finishWithPendingAuthError()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val authUrl: String? = methodCall.argument("authUrl")
            if (authUrl != null) {
                val result = withContext(Dispatchers.IO) {
                    executeAuthTask(authUrl)
                }
                finishWithSuccess(result)
            } else {
                finishWithError("AUTH_URL_NULL", "Authorization URL is null")
            }
        }
    }

    private suspend fun executeAuthTask(authUrl: String): String {
        return suspendCoroutine { continuation ->
            val authSingleton = AuthSingleton.instance
            authSingleton.url = authUrl
            Log.e("AuthDelegate", "Starting authorization with URL: $authUrl")

            val activity = WeakReference(activity).get()
            if (activity != null) {
                val intent = Intent(activity, AuthActivity::class.java)
                activity.startActivity(intent)

                synchronized(authSingleton) {
                    try {
                        (authSingleton as java.lang.Object).wait() // Awaiting the result
                    } catch (e: InterruptedException) {
                        continuation.resume(authSingleton.responseJson)
                    }
                }
            }
            continuation.resume(authSingleton.responseJson)
        }
    }

    private fun setPendingResult(result: MethodChannel.Result): Boolean {
        return if (pendingResult == null) {
            pendingResult = result
            true
        } else {
            false
        }
    }

    private fun finishWithSuccess(webResponse: String) {
        Log.e("AuthDelegate", "finishWithSuccess: $webResponse")
        pendingResult?.success(webResponse)
        clearResult()
    }

    private fun finishWithPendingAuthError() {
        finishWithError("pending_authorization", "Authentication is already pending")
    }

    private fun finishWithError(errorCode: String, errorMessage: String) {
        pendingResult?.error(errorCode, errorMessage, null)
        clearResult()
    }

    private fun clearResult() {
        pendingResult = null
    }
}

interface OnAuthCompleteListener {
    fun onComplete(webResponse: String)
}
