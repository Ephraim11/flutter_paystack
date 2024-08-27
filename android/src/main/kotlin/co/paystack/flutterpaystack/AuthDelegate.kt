package co.paystack.flutterpaystack

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

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

        val authUrl: String? = methodCall.argument("authUrl")
        if (authUrl != null) {
            AuthExecutor(WeakReference(activity), WeakReference(onAuthCompleteListener)).execute(authUrl)
        } else {
            finishWithError("invalid_auth_url", "Authorization URL is missing")
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
        Log.e("AuthDelegate", "finishWithSuccess (line 44): $webResponse")
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

private class AuthExecutor(
    private val activityRef: WeakReference<Activity>,
    private val listenerRef: WeakReference<OnAuthCompleteListener>
) {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun execute(authUrl: String) {
        executor.execute {
            val authSingleton = AuthSingleton.instance
            authSingleton.url = authUrl
            Log.e("AuthExecutor", "Executing Auth: ${authSingleton.url}")

            val activity = activityRef.get()
            if (activity != null) {
                val intent = Intent(activity, AuthActivity::class.java)
                activity.startActivity(intent)

                synchronized(authSingleton) {
                    try {
                        (authSingleton as Object).wait()
                    } catch (e: InterruptedException) {
                        return@execute
                    }
                }
            }

            val responseJson = authSingleton.responseJson

            handler.post {
                listenerRef.get()?.onComplete(responseJson)
            }
        }
    }
}

interface OnAuthCompleteListener {
    fun onComplete(webResponse: String)
}
