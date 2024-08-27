package co.paystack.flutterpaystack

import android.app.Activity
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class AuthDelegate(private val activity: Activity) {

    private var pendingResult: MethodChannel.Result? = null

    fun handleAuthorization(pendingResult: MethodChannel.Result, methodCall: MethodCall) {
        if (!setPendingResult(pendingResult)) {
            finishWithPendingAuthError()
            return
        }
        val authUrl: String? = methodCall.argument("authUrl")
        if (authUrl != null) {
            AuthCoroutineTask(WeakReference(activity), WeakReference(onAuthCompleteListener)).execute(authUrl)
        } else {
            finishWithError("auth_url_error", "Authorization URL is missing")
        }
    }

    private val onAuthCompleteListener = object : OnAuthCompleteListener {
        override fun onComplete(webResponse: String) {
            finishWithSuccess(webResponse)
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

private class AuthCoroutineTask(
    private val activityRef: WeakReference<Activity>,
    private val listenerRef: WeakReference<OnAuthCompleteListener>
) {

    fun execute(authUrl: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val responseJson = withContext(Dispatchers.IO) {
                performAuthorization(authUrl)
            }
            listenerRef.get()?.onComplete(responseJson)
        }
    }

    private suspend fun performAuthorization(authUrl: String): String {
        val authSingleton = AuthSingleton.instance
        authSingleton.url = authUrl
        Log.e("AuthCoroutineTask", "performAuthorization: ${authSingleton.url}")

        val activity = activityRef.get()
        if (activity != null) {
            val i = Intent(activity, AuthActivity::class.java)
            activity.startActivity(i)

            return suspendCancellableCoroutine { continuation ->
                synchronized(authSingleton) {
                    try {
                        (authSingleton as Object).wait()
                    } catch (e: InterruptedException) {
                        continuation.resume(authSingleton.responseJson) {}
                    }
                }
                continuation.resume(authSingleton.responseJson) {}
            }
        }

        return authSingleton.responseJson
    }
}

interface OnAuthCompleteListener {
    fun onComplete(webResponse: String)
}
