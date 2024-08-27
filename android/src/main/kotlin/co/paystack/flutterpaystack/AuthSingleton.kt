package co.paystack.flutterpaystack

/**
 * Singleton class to hold authorization response and URL.
 * Created by Wilberforce on 29/07/18 at 06:00.
 */
object AuthSingleton {
    @Volatile
    var responseJson: String = "{\"status\":\"requery\",\"message\":\"Reaffirm Transaction Status on Server\"}"
    @Volatile
    var url: String = ""
}
